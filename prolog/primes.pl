init(MAX_N) :- build(2, MAX_N).

build(V, N) :- V2 is V * V, V2 > N, !.
build(V, N) :- \+ composite(V), V2 is V * 2, assert(min_div(V, V)), mark(V2, V, N), fail.
build(V, N) :- V2 is V + 1, build(V2, N).

mark(V, S, N) :- V > N, !.
mark(V, S, N) :- \+ composite(V), assert(composite(V)), assert(min_div(V, S)), fail.
mark(V, S, N) :- V2 is V + S, mark(V2, S, N).

prime(N) :- N > 1, \+ composite(N).

prime_divisors(1, []) :- !.
prime_divisors(V, [V]) :- prime(V), !.
prime_divisors(V, [R | T]) :- number(V), min_div(V, R), New_V is div(V, R), prime_divisors(New_V, T), !.
prime_divisors(R, [V, U | T]) :- V =< U, prime(V), prime_divisors(R2, [U | T]), R is R2 * V.

prime_palindrome(N, K) :- prime(N), convert_number(N, K, Conv_N), palindrome(Conv_N).

convert_number(N, K, [R]) :- 0 is div(N, K), R is N mod K, !.
convert_number(N, K, [R | T]) :- New_N is div(N, K), R is N mod K, convert_number(New_N, K, T), !.
convert_number(0, _, [0]).

palindrome(VAL) :- reverse(VAL, REV_VAL), VAL = REV_VAL.