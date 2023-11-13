"use strict";

function Operation(...operators){
    this._operators = operators;
}

Operation.prototype.evaluate = function(...args){
    return this._function(...this._operators.map(val => val.evaluate(...args)))
}

Operation.prototype.toString = function(){
    return [...this._operators, this._symbol].join(' ');
}

Operation.prototype.prefix = function(){
    return '(' + [this._symbol, ...this._operators.map(val => val.prefix())].join(' ') + ')';
}
Operation.prototype.postfix = function(){
    return '(' + [...this._operators.map(val => val.postfix()), this._symbol].join(' ') + ')';
}
Operation.prototype.diff = function(value){
    return this._diffFunction(...this._operators, ...this._operators.map(val => val.diff(value)));
}

function MakeOperation(f, df, symb){
    function op(...args){
        Operation.call(this, ...args)
    }
    op.prototype = Object.create(Operation.prototype)
    op.prototype._diffFunction = df
    op.prototype._function = f
    op.prototype._symbol = symb
    op.prototype.constructor = op
    Object.defineProperty(op, "length", {value: f.length})
    return op
}

const TWO = new Const(2)
const ONE = new Const(1)
const ZERO = new Const(0)
const E = new Const(Math.E)
const Ln = (val) => new Log(E, val)
const Square = (val) => new Pow(val, TWO)

function Variable(value) {
    this.value = value;
}

Variable.prototype.evaluate = function (...args){ return args[VARS[this.value]]; }
Variable.prototype.toString = function (){ return this.value.toString(); }
Variable.prototype.diff = function (val){ return val === this.value ? ONE : ZERO; }
Variable.prototype.prefix = function (){ return this.toString() }
Variable.prototype.postfix = function (){ return this.toString() }

function Const(value) {
    this.value = value;
}

Const.prototype.evaluate = function (){ return this.value; }
Const.prototype.toString = function (){ return this.value.toString(); }
Const.prototype.diff = function (){ return ZERO; }
Const.prototype.prefix = function (){ return this.toString() }
Const.prototype.postfix = function (){ return this.toString() }

const Subtract = MakeOperation((a, b) => a - b, (a, b, da, db) => new Subtract(da, db), '-');
const Add = MakeOperation((a, b) => a + b, (a, b, da, db) => new Add(da, db), '+');
const Multiply = MakeOperation((a, b) => a * b, (a, b, da, db) => new Add(
    new Multiply(da, b), 
    new Multiply(db, a)
), '*');
const Divide = MakeOperation((a, b) => a / b, (a, b, da, db) => new Divide(
    new Subtract(new Multiply(da, b), new Multiply(db, a)), 
    Square(b)
), '/');
const Negate = MakeOperation((a) => -a, (a, da) => new Negate(da), 'negate');
const Pow = MakeOperation(Math.pow, (a, b, da, db) => new Multiply(
    new Pow(a, b), 
    new Add(
        new Divide(new Multiply(da, b), a), 
        new Multiply(db, Ln(a))
    )
), 'pow');
const Log = MakeOperation((a, b) => Math.log(Math.abs(b)) / Math.log(Math.abs(a)), (a, b, da, db) => new Divide(
    new Subtract(
        new Divide(new Multiply(db, Ln(a)), b),
        new Divide(new Multiply(da, Ln(b)), a)
    ), 
    Square(Ln(a))
), 'log');

const ALL_VALUES = -1

function MakeSeveralArgumentsOperation(...args){
    let operation = MakeOperation(...args)
    Object.defineProperty(operation, "length", {value: ALL_VALUES})
    return operation
}

const Mean = MakeSeveralArgumentsOperation((...args) => args.reduce((prev, cur) => prev + cur) / args.length, 
            (...args) => new Divide(
                    args.slice(-args.length / 2).reduce((prev, cur) => new Add(prev, cur), ZERO) , 
                    new Const(args.length / 2)
                ), 'mean');
const Var = MakeSeveralArgumentsOperation((...args) => {
    return args.reduce((prev, cur) => prev + Math.pow(cur, 2), 0) / args.length 
                            - Math.pow(args.reduce((prev, cur) => prev + cur), 2) / Math.pow(args.length, 2);
}, (...args) => {
    let n = args.length / 2
    let vals = args.slice(0, n)
    let dvals = args.slice(-n)
    let dsquare_sum = vals.reduce((prev, cur, index) => new Add(prev, new Multiply(TWO, new Multiply(cur, dvals[index]))), ZERO)
    let dsum = new Multiply(
        new Multiply(vals.reduce((prev, cur) => new Add(prev, cur), ZERO), TWO), 
        dvals.reduce((prev, cur) => new Add(prev, cur), ZERO)
    )
    return new Subtract(new Divide(dsquare_sum, new Const(n)), new Divide(dsum, new Const(Math.pow(n, 2))))
}, 'var')
const OPERATIONS = {'-' : Subtract, '*' : Multiply, '+' : Add, '/' : Divide, 'negate': Negate, 'pow': Pow, 'log': Log, 'mean': Mean, 'var': Var};
const VARS = {'x' : 0, 'y' : 1, 'z' : 2}

const tokenParser = (data, val) => {
    if (val in VARS){ 
        data.push(new Variable(val))
    } else if (val in OPERATIONS) {
        data.push(new OPERATIONS[val](...data.splice(data.length - OPERATIONS[val].length, OPERATIONS[val].length)))
    } else {
        data.push(new Const(+val))
    }
    return data
};
const parse = (str) => str.split(" ").filter(val => val.length > 0).reduce(tokenParser, []).pop();

const checkWhitespace = function(val){
    return val.trim() === val && val.length > 0
}

function ParserError(message){
    Error.call(this, message);
    this.message = message;
}
ParserError.prototype = Object.create(Error.prototype);
ParserError.prototype.constructor = ParserError;
ParserError.prototype.name = "ParserError";

const ParserContainer = function (isPrefix){
    this.isPrefix = isPrefix
    this._operations = []
    this._values = []
    this._val_count = [0]
    this._depth = 0
}

ParserContainer.prototype.addContext = function (){
    this._val_count.push(0)
    this._depth += 1
}

ParserContainer.prototype.closeContext = function (){
    if(this._depth == 0){
        throw new ParserError("Missing (")
    }
    if(this._operations.length != this._depth){
        throw new ParserError("No operation in context")
    }

    let op = this._operations.pop()
    let argc = OPERATIONS[op].length == ALL_VALUES ? this._val_count[this._depth] : OPERATIONS[op].length;
    if(argc > this._val_count[this._depth]){
        throw new ParserError("Not enough arguments")
    }else if(argc < this._val_count[this._depth]){
        throw new ParserError("Too many arguments")
    }
    this._values.push(new OPERATIONS[op](...this._values.splice(this._values.length - argc, argc)))
    this._val_count.pop()
    this._val_count[this._val_count.length - 1] += 1
    this._depth -= 1
}

ParserContainer.prototype.addValue = function (value){
    if(!this.isPrefix && this._operations.length == this._depth && this._depth != 0){
        throw new ParserError("Operation already exists in this context")
    }
    this._values.push(value)
    this._val_count[this._depth] += 1
}

ParserContainer.prototype.addOperation = function (value){
    if(this.isPrefix && this._val_count[this._depth] != 0){
        throw new ParserError("Operation already exists in this context")
    }
    this._operations.push(value)
}

ParserContainer.prototype.getResult = function (){
    if(this._depth != 0){
        throw new ParserError("Missing )")
    }
    if(this._values.length == 0){
        throw new ParserError("Empty input")
    }
    if(this._operations.length != 0 || this._values.length != 1){
        throw new ParserError("Excessive info")
    }
    return this._values.pop()
}

const parseToken = (data, val) => {
    if(val === '('){
        data.addContext();
    }else if(val === ')'){
        data.closeContext();
    }else if (val in VARS){ 
        data.addValue(new Variable(val))
    } else if (val in OPERATIONS) {
        data.addOperation(val)
    } else if(!isNaN(+val)){
        data.addValue(new Const(parseFloat(val)))
    }else{
        throw new ParserError("Unexpected token")
    }
    return data
}

const SEPARATORS = ['(', ')', ' ']

const tokenSplitter = (str) => {
    return str.split('').reduce((prev, cur) => {
        if(SEPARATORS.includes(cur)){
            prev.push(cur)
            prev.push('')
        }else{
            prev[prev.length - 1] += cur
        }
        return prev
    }, [''])
}

const getParsed = (str, isPrefix) => {
    return tokenSplitter(str).filter(checkWhitespace).reduce(parseToken, new ParserContainer(isPrefix)).getResult()
}
const parsePrefix = (str) => getParsed(str, true);
const parsePostfix = (str) => getParsed(str, false);
