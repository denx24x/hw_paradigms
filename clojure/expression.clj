(def constant constantly)
(def variable (fn [val] (fn [data] (get data val))))

(defn operation [f]
    (fn [& operands]
    (fn [data] (apply f (mapv #(% data) operands)))))

(def add (operation +))
(def subtract (operation -))
(def multiply (operation *))
(defn correct-div 
    ([val] (/ 1 (double val)))
    ([first & other] (/ first (double (apply * other))))
)
(def divide (operation correct-div))
(def negate subtract)
(def mean (operation (fn [& args] (/ (apply + args) (count args)))))
(def varn (operation (fn [& args]
    (let [
            sum (apply + args)
            n (count args)
            square-sum (apply + (mapv #(* % %) args))
        ]
        (- (/ square-sum n) (* (/ sum n) (/ sum n)))
    )
)))

(def OPERATIONS_FUNCTIONAL {
    '+ add
    '- subtract
    '* multiply
    '/ divide
    'negate negate
    'mean mean 
    'varn varn
})

(defn parserGenerator [OPS CONST VAR]
    (fn [str]
        (letfn [(parse [val]
                    (cond 
                        (list? val) (apply (get OPS (first val)) (mapv parse (rest val)))
                        (number? val) (CONST val)
                        :else (VAR (name val))
                    )
                )]
        (parse (read-string str)))
    )
)
(def parseFunction (parserGenerator OPERATIONS_FUNCTIONAL constant variable))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 10

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 11
(load-file "proto.clj")

(def evaluate (method :evaluate))
(def toString (method :toString))
(def toStringInfix (method :toStringInfix))
(def toStringSuffix (method :toStringSuffix))
(def diff (method :diff))
(def _function (field :function))
(def _diffFunction (field :diffFunction))
(def _symbol (field :symbol))
(def _operands (field :operands))
(def _value (field :value))

(def OperationProto {
    :evaluate (fn [this data] (apply (_function this) (mapv #(evaluate % data) (_operands this))))
    :toString (fn [this] (format "(%s %s)" (_symbol this) (clojure.string/join " " (mapv toString (_operands this)))))
    :toStringSuffix (fn [this] (format "(%s %s)" (clojure.string/join " " (mapv toStringSuffix (_operands this))) (_symbol this)))
    :toStringInfix (fn [this] (cond 
                            (= 2 (count (_operands this))) (format "(%s %s %s)" (toStringInfix (first (_operands this))) (_symbol this) (toStringInfix (first (rest (_operands this)))))
                            (= 1 (count (_operands this))) (format "%s(%s)" (_symbol this) (toStringInfix (first (_operands this))))
                            ))
    :diff (fn [this data] ((_diffFunction this) (_operands this) (mapv #(diff % data) (_operands this))))
})
(defn Operation [this & operands] (assoc this :operands operands))

(defn MakeOperation [f df symb] (constructor Operation (assoc OperationProto 
    :diffFunction df 
    :function f
    :symbol symb
)))

(declare Constant ZERO ONE)
(def ConstProto {
    :evaluate (fn [this data] (_value this))
    :toString (fn [this] (str (_value this)))
    :toStringSuffix toString
    :toStringInfix toString
    :diff (fn [this value] ZERO)
})
(def Constant (constructor (fn [this value] (assoc this :value value)) ConstProto))
(def TWO (Constant 2))
(def ONE (Constant 1))
(def ZERO (Constant 0))

(def VariableProto {
    :evaluate (fn [this data] (get data (_value this)))
    :toString (fn [this] (_value this))
    :toStringSuffix toString
    :toStringInfix toString
    :diff (fn [this value] (if (= value (_value this)) ONE ZERO))
})
(def Variable (constructor (fn [this value] (assoc this :value value)) VariableProto))

(declare Add Subtract Multiply Divide Negate Mean Varn)
(def Add (MakeOperation + (fn [vars dvars] (apply Add dvars)) "+"))
(def Subtract (MakeOperation - (fn [vars dvars] (apply Subtract dvars)) "-"))

(defn mul-dif [vars dvars] 
    (let 
        [mul (apply Multiply vars)] 
        (apply Add (map-indexed 
                    (fn [ind val] 
                        (Divide (Multiply mul val) (nth vars ind))) 
                    dvars)
        )
    )
)
(def Multiply (MakeOperation * mul-dif "*"))
(defn Square [x] (Multiply x x))

(defn single-div-dif [v dv] )
(defn div-dif [vars dvars] 
    (let 
        [other (apply Multiply (rest vars))]
    (cond 
        (= (count vars) 1) (Negate (Divide (first dvars) (Square (first vars))))
        :else (Divide 
                (Subtract 
                    (Multiply (first dvars) other) 
                    (Multiply (first vars) (mul-dif (rest vars) (rest dvars)))
                ) 
                (Multiply other other))
            )
    ))
(def Divide (MakeOperation correct-div div-dif "/"))
(def Negate (MakeOperation - (fn [vars dvars] (apply Negate dvars)) "negate"))

(defn mean-func [& args] (/ (apply + args) (count args)))
(defn mean-dif [vars dvars] (Divide (apply Add dvars) (Constant (count dvars))))
(def Mean (MakeOperation mean-func mean-dif "mean"))

(defn varn-func [& args] 
    (let [
            sum (apply + args) 
            n (count args)
        ] 
        (- 
            (/ (apply + (mapv #(* % %) args)) n) 
            (/ (* sum sum) (* n n))
        )
    )
)
(defn varn-dif [vars dvars] 
    (let [
            n (Constant (count vars))
            dsquare_sum (apply Add (mapv (fn [v dv] (Multiply TWO v dv)) vars dvars))
            dsum (Multiply (apply Add vars) (apply Add dvars) TWO)
        ]
        (Subtract 
            (Divide dsquare_sum n) 
            (Divide dsum (Square n))
        )
    )
)
(def Varn (MakeOperation varn-func varn-dif "varn"))

(def OPERATIONS_OBJECT {
    '+ Add
    '- Subtract
    '* Multiply
    '/ Divide
    'negate Negate
    'mean Mean
    'varn Varn
})

(def parseObject (parserGenerator OPERATIONS_OBJECT Constant Variable)) 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
(load-file "parser.clj")

(def *all-chars (mapv char (range 32 128)))
(def *digit (+char "0123456789"))
(def *letter (+char (apply str (filter #(Character/isLetter %) *all-chars))))
(def *space (+char " \t\n\r"))
(def *ws (+ignore (+star *space)))
(def *identifier (+str (+seqf cons *letter (+star (+or *letter *digit)))))
(defn construct-number [s int frac]
    (let [tail (if (nil? frac) int (cons int frac))]
    (if (= \- s) (cons s tail) tail)
    )
)
(def *string
    (+seqn 1
           (+char "\"")
           (+str (+star (+char-not "\"")))
           (+char "\"")))
(def *number 
    (+map read-string (+map clojure.string/join (+seqf construct-number
        (+opt (+char "-"))
        (+str (+plus *digit))
        (+str (+opt
            (+seqf cons
                (+char ".")
                (+plus *digit)
            )
        ))
    ))))

(defn *check-string [string] 
    (apply +seq (map #(+char (str %)) string))
)

(def *constant (+map Constant (+seqn 0 *ws *number *ws)))

(def *variable (+map Variable (+seqn 0 *ws (+str (+plus *letter)) *ws)))

(def *operation
    (+map #(get OPERATIONS_OBJECT (symbol %)) (apply +or (map (fn [[k v]] (+str (*check-string (name k)))) OPERATIONS_OBJECT)))
)

(defn construct-operation [& args] (apply (last args) (drop-last args)))

(def *expression 
    (+or
        (+seqn 1 *ws (+char "(") (+seqf construct-operation *ws (delay *expression) *ws *operation *ws) (+char ")") *ws)
        (+seqn 1 *ws (+char "(") (+seqf construct-operation *ws (delay *expression) *ws (delay *expression) *ws *operation *ws) (+char ")") *ws)
        *constant
        *variable 
    )
)
;(def parse-const (+))
(def parseObjectSuffix (+parser *expression))
;(def parseObjectSuffix )

(defn construct-operation-infix 
    [arg1 op arg2] (op arg1 arg2)
)
(defn construct-operation-infix-single
    [op arg] (op arg)
)
(declare *expression-infix *expression-infix-single)
(def *expression-infix-single
    (+seqn 0
        *ws
        (+or
            (+seqf construct-operation-infix-single
                *operation
                *ws
                (+ignore (+char "("))
                (delay *expression-infix-single)
                (+ignore (+char ")"))
            ) 
            (+seqn 0
                (+ignore (+char "("))
                (delay *expression-infix)
                (+ignore (+char ")"))    
            )
            *constant
            *variable
        )
        *ws
    )
)

(def *expression-infix 
    (+seqn 0
        *ws
        (+or
            (+seqf construct-operation-infix
                (delay *expression-infix-single)
                *ws
                *operation
                *ws
                (delay *expression-infix)
            )
            *expression-infix-single
        )
        *ws
    )
)
;(def parse-const (+))
(def parseObjectInfix (+parser *expression-infix))
(println (toStringInfix (parseObjectInfix "x*x+5.0* (z*(  z*z ))  -y*8.0")))