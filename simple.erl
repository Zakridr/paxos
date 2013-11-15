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
    if MyCoin =:= Num -> io:format("~p: I'm going to propose~n", [self()]),
                         propose(PList);
       MyCoin =/= Num -> io:format("~p: I'm going to accept~n", [self()]),
                         accept(blank, blank)
    end.

% what args does acceptor need?
propose(PList) ->
    io:format("pid ~p: I'm going to propose~n", [self()]),
    NumPriests = length(PList) + 1,
    ProposalNumber = getpropnum(),
    TargetAcceptors = getquorum(PList),
    ResponsesCountdown = NumPriests div 2 + NumPriests rem 2,
    lists:foreach(fun(AccPid) -> AccPid ! {self(), {prepare_request, ProposalNumber}} end, TargetAcceptors),
    proposeprepare(ProposalNumber, TargetAcceptors, ResponsesCountdown, blank).

% wait for majority to respond
proposeprepare(ProposalNumber, TargetAcceptors, ResponsesCountdown, Value) ->
    if ResponsesCountdown =:= 0 -> proposecommit(ProposalNumber, TargetAcceptors, Value);
       ResponsesCountdown =/= 0 ->
           % need to check pids! make sure we're not receiving duplicate messages
           receive 
               % we get blank value back - use our value
               {_, {promise, {_, blank}}} -> 
                   proposeprepare(ProposalNumber, TargetAcceptors, ResponsesCountdown - 1, Value);
               % we get an actual value back
               {_, {promise, {PropNum, V}}} ->
                   % right now, this doesn't check the PropNum ... 
                   proposeprepare(ProposalNumber, TargetAcceptors, ResponsesCountdown - 1, V);
               {_, {sorry}} -> 
                   % abort proposal!
                   io:format("pid ~p: aborting proposal~n", [self()])
           end
    end.

proposecommit(PropNum, Acceptors, Value) ->
                     %pick value here... needs to change
    MyValue = if Value =:= blank -> self();
                 Value =/= blank -> Value
              end,
    lists:foreach(fun (AccPid) -> AccPid ! {self(), {accept_request, {PropNum, MyValue}}} end, Acceptors),
    io:format("proposer ~p: just finished, I think value is: ~p~n", [self(), MyValue]),
    accept(PropNum, Value).

accept(HighestPropNum, ValueAccepted) ->
    receive
        % prepare requests
        {Pid, {prepare_request, ProposalNumber}} when HighestPropNum =< ProposalNumber ->
            Pid ! {self(), {promise, {HighestPropNum, ValueAccepted}}},
            accept(ProposalNumber, ValueAccepted);
        {Pid, {prepare_request, _}} ->
            Pid ! {self(), {sorry}},
            accept(HighestPropNum, ValueAccepted);

        % accept requests
        {_, {accept_request, {Pnum, V}}} when HighestPropNum =< Pnum ->
            io:format("acceptor ~p: I think value is ~p~n", [self(), V]),
            accept(Pnum, V);
        {Pid, {accept_request, _}} ->
            Pid ! {self(), {sorry}},
            accept(HighestPropNum, ValueAccepted)
    end.

% comparison of records is lexicographic (like a dictionary)
% we compare proposal numbers first by now(), then by the reference
getpropnum() ->
    {now(), make_ref()}.

% just returns the entire list for now
getquorum(PList) ->
    PList.
