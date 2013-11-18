paxos
=====
IMPORTANT:
Download sbt.
http://www.scala-sbt.org/

I separated out the classes into separate files to make things more manageable.
I think we have a reasonable starting point now, I want to organize things a
little before going on to do multi-paxos. So when I tried to run scalac, I had
complaints based on whether certain files were compiled into .class files yet
or whatever. So I did some research and decided to use sbt as the build tool to
manage compilation. It seems to be a little complicated, but for basic use it's
pretty simple. Just use the following commands, and you should see the app guy
run:
sbt (from terminal, in project main directory)
run (from sbt's console)

Paxos specific:

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
