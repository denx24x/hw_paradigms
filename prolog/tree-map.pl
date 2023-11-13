
%cmp(A, inf).
%cmp(A, B) :- A =< B.

map_build((K, V), value(K, V)).

check_find([(K1, V) | T], K, R) :- K =< K1,!, find(V, K, R).
check_find([(K1, V1) | T], K, R) :- check_find(T, K, R).
 
find(value(K, V), K, V).
find(node(Data), K, R) :- check_find(Data, K, R). 

is_value(value(K, V)).
is_values([]).
is_values([(K, V) | T]) :- is_value(V), is_values(T).

merge_lists([], L, L).
merge_lists([H | T], L ,[H | T2]):- merge_lists(T, L, T2).

div(L, A, B) :- split(L, L, A, B).

split(B, [], [], B).
split([H|T], [_, _|T1], [H | T2], B) :- split(T, T1, T2, B).
   
add_node(node(Data1), node(Data2), node(Data3)) :- merge_lists(Data1, Data2, Data3).

split_node(node(Data), [node(Data)]) :- length(Data, L), L =< 3.
split_node(node(Data), [node(Data1), node(Data2)]) :- split(Data, D1, D2), reorder(D1, Data1), reorder(D2, Data2).

cmp_node((K1, V1), (K2, V2)) :- K1 =< K2.
perm([],[]).
perm(L,[H|T]) :-
 append(V,[H|U],L),
 append(V,U,W), perm(W,T).
inOrder([]).
inOrder([_]).
inOrder([A,B|T]) :-
 cmp_node(A, B), inOrder([B|T]).
 sort(L1,L2) :-
 perm(L1,L2), inOrder(L2).
reorder(node(Data), node(Data2)) :- sort(Data, Data2).



check_insert([(K1, V) | T], (K, V), [R | T]) :- (K =< K1; length(T, L), L is 0), !, insert(V, (K, V), R2), split_node(R2, R).
check_insert([(K1, V1) | T], (K, V), [(K1, V1) | R]) :- check_insert(T, (K, V), R).

insert(value(K1, V1), (K, V), R) :- R = node([(K1, value(K1, V1)), (K, value(K, V))]), !. 
insert(node(Data), (K, V), R) :- is_values(Data), add_node(node(Data), node([(K, value(K, V))]), R), !.
insert(node(Data), (K, V), R) :- check_insert(Data, (K, V), R). 