-module(simple).
-compile(export_all).

-define(TIMEOUT, 500).
% first thing's first, spawn a bunch of processes and make them aware of each other
%

init(NumProcs) ->
    init_interior(NumProcs, []).


% for i = 0 to NumProcs
%   create new process, stick into list
% loop is done here
% tell every process in list pid of every process
% (send a message with pid list to every process)
init_interior(0, PList) ->
    lists:foreach(fun(Pid) -> Pid ! {self(), {all_priests, lists:delete(Pid, PList)}} end, PList);
init_interior(NumProcs, PList) ->
    init_interior(NumProcs - 1, [spawn(?MODULE, priest_init, []) | PList]).

% initialize random seeds here?
% randomness looks bad. incorporate pid somehow
priest_init() ->
    random:seed(now()),
    receive
        {_, {all_priests, PList}} -> priest(PList)
    end.


%priest() ->
    % toss coin
    % if heads, be acceptor/learner
    % if tails, be proposer
% rewrite to use coin toss function
priest(PList) ->
    Num = length(PList),
    MyCoin = random:uniform(Num),
    if MyCoin =:= Num -> propose(PList);
       MyCoin =/= Num -> accept()
    end.

% what args does acceptor need?
propose(PList) ->
    ProposalNumber = getpropnum(self()),
    TargetAcceptors = getquorum(PList),
    lists:foreach(fun(AccPid) -> AccPid ! {self(), {prepare_request, ProposalNumber}} end, TargetAcceptors),
    proposephase2(ProposalNumber, TargetAcceptors).

proposephase2(ProposalNumber, TargetAcceptors) ->
    io:format("pid ~p: got to propose phase 2 with prop num ~p, acceptors ~p~n", [self(), ProposalNumber, TargetAcceptors]).


accept() ->
    io:format("I am an acceptor~n").

getpropnum(Pid) ->
    {Pid, now()}.

% just returns the entire list for now
getquorum(PList) ->
    PList.
