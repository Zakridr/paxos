paxos
=====

So right now, it should always select a leader.  The servers keep restarting
(calling the startround() function) when they timeout, or when a proposer finds
out there's another proposer with a higher proposal number, or something like
that.

I guess the next step is to do multi-step paxos, and think about how we're
going to demonstrate the algorithm. Or figure out how to do model errors.  For
message delays, we could use a messenger class to send all messengers, that
tosses coins and determines whether to send the message or not, or whether to
add some latency, or whatever. For crashes, I'm not quite sure how to do that.
Maybe have a crasher actor, that sends a special crash message, and when a
server receives that then they stop whatever they're doing and sleep for a bit.
But then we couldn't model crashes in between certain events, for example after
a proposer has started sending some messages but before he finishes sending
them all.
