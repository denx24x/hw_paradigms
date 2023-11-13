const op = (f) => Object.defineProperty((...arg) => (x, y, z) => f(...arg.map(calc => calc(x, y, z))), "length", {value: f.length})


const variable = (name) => (x, y, z) => ({"x" : x, "y" : y, "z" : z}[name])
const add = op((a, b) => a + b)
const subtract = op((a, b) => a - b)
const multiply = op((a, b) => a * b)
const divide = op((a, b) => a / b)
const negate = op((value) => -value)
const cnst = (value) => (x, y, z) => value
const sinh = op((value) => (Math.pow(e, value) - Math.pow(e, -value)) / 2)
const cosh = op((value) => (Math.pow(e, value) + Math.pow(e, -value)) / 2)
const ops = {"*": multiply, "+": add, "-": subtract, "/": divide,
       "negate" : negate, "cosh" : cosh, "sinh" : sinh}
const vars = ["x", "y", "z"]
const one = cnst(1)
const two = cnst(2)
const cnsts = {"one" : one, "two" : two}
function parser(stack, val){
	if (val in ops){
		stack.push(ops[val](...stack.splice(stack.length - ops[val].length, ops[val].length)))
	}else if(vars.includes(val)){
		stack.push(variable(val))
	}else if(val in cnsts){
		stack.push(cnsts[val])
	}else{
		stack.push(cnst(Number(val)))
	}
	return stack
}
const parse = (expression) => expression.split(" ").filter(n => n.length > 0).reduce(parser, []).pop();


/* ~~~~~~~~~~~TEST PROGRAMM CODE~~~~~~~~~~~ /*
let expr = parse("x x * 2 x * - 1 +") // x^2−2x+1 
for(let i = 0; i <= 10; i++){
  println(expr(i, 0, 0))
}
*/