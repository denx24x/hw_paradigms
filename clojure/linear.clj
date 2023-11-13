(defn generator [f] (fn [& arg] (apply mapv f arg)))

(defn vector-equal [& arg] (apply = (map count arg)))
(defn vector-condition [& arg] 
    (and 
        (every? vector? arg) 
        (every? #(every? number? %) arg)
        (apply vector-equal arg)
    )
)

(defn vector-generator [f arg] 
    {:pre [(apply vector-condition arg)]
     :post [(vector-condition %)]} 
    (apply (generator f) arg))

(defn v+ [& arg] (vector-generator + arg))
(defn v- [& arg] (vector-generator - arg))
(defn v* [& arg] (vector-generator * arg))
(defn vd [& arg] (vector-generator / arg))

(defn scalar [& arg] 
    {:pre [(apply vector-condition arg)]
    :post [(number? %)]} 
    (apply + (apply v* arg)))
(defn vect [& arg] 
    {:pre [(apply vector-condition arg) (= 3 (count (first arg)))]
     :post [(vector-condition %) (= 3 (count %))]}
    ( letfn [(vect-single [a, b] 
        (letfn [(calc [fpos, spos] (- (* (nth a fpos) (nth b spos)) (* (nth a spos) (nth b fpos))))]
        (vector (calc 1 2) (- (calc 0 2)) (calc 0 1))))]
    (reduce vect-single arg))
)
(defn v*s [v & arg] 
    {:pre [(vector-condition v) (every? number? arg)]
     :post [(vector-condition %)]} 
    ((generator #(* % (reduce * arg))) v))

(defn matrix-condition [& arg]
    (and 
        (every? vector? arg) 
        (every? #(every? vector-condition %) arg)
    )
)
(defn matrix-equals [& arg] 
    (and
        (apply = (map count arg))
        (every? vector-equal arg)
        (apply = (map #(count (first %)) arg))
    )
)
(defn matrix-generator [f arg] 
    {:pre [(apply matrix-condition arg) (apply matrix-equals arg)]
     :post [(matrix-condition %)]} 
    (apply (generator f) arg))

(defn m+ [& arg] (matrix-generator v+ arg))
(defn m- [& arg] (matrix-generator v- arg))
(defn m* [& arg] (matrix-generator v* arg))
(defn md [& arg] (matrix-generator vd arg))

(defn m*s [m & arg] 
    {:pre [(matrix-condition m) (every? number? arg)]
     :post [(matrix-condition %)]}
    ((generator #(v*s % (reduce * arg))) m))
(defn m*v [m & arg] 
    {:pre [(matrix-condition m) (every? vector-condition arg)]
     :post [(vector-condition %)]} 
    ((generator #(reduce + (v* % (reduce v* arg)))) m))
(defn transpose [m] 
    {:pre [(matrix-condition m)]
     :post [(matrix-condition %)]}
    (apply mapv vector m)) 
(defn m*m [& arg] 
    {:pre [(apply matrix-condition arg)]
     :post [(matrix-condition %)]} 
    (reduce (fn [a, b] (mapv #(m*v (transpose b) %) a)) arg))


(defn simplex [f]
    (letfn [(calc [& arg]
        (cond
            (every? number? arg) (apply f arg)
            :else (apply mapv calc arg)
        )
    )] calc)
)
    
(defn simplex-condition [arg] 
    (letfn [
        (check [arg] 
        (cond 
            (empty? arg) false
            (every? number? arg) true
            (every? vector? arg) (and 
                (apply > (mapv count arg)) 
                (= (count (first arg)) (count arg)) 
                (every? identity (mapv check arg))
            )
            :else false
        ))]
    (and (vector? arg) (check arg))))

(defn simplex-equals [& arg] 
    (letfn [(check [& arg] 
        (cond 
            (every? number? arg) true
            :else (and 
                (apply = (mapv count arg))  
                (every? identity (apply mapv check arg))
            )
        ))]
    (apply check arg)))

(defn simplex-generator [f arg] 
    {:pre [(every? simplex-condition arg) (apply simplex-equals arg)]
    :post [(simplex-condition %)]} 
    (apply (generator (simplex f)) arg))
(defn x+ [& arg] (simplex-generator + arg))
(defn x* [& arg] (simplex-generator * arg))
(defn x- [& arg] (simplex-generator - arg))
(defn xd [& arg] (simplex-generator / arg))
