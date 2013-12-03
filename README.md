
sbt
=====
Download sbt.
http://www.scala-sbt.org/

To compile + run, just execute sbt in the project directory.
Then in the sbt shell, type run. This should run the election class.

organization
=====
All source files are in src/main/scala


stand-alone jar
=====
In the project directory, run sbt.
Then type:
reload
compile
assembly

This produces a jar in the folder target/scala-2.10/
You can run the jar with "java -jar \<jarname\>"


paxos
=====
Now I finished the multipaxos, For simplicity, there is no leader selection round and we
have only single leader, I believe this can be extened to multiple leaders with minimum change but we have
to take leader contention into consideration in this case.

There are several classes used for msg transformation and msg manipulation:

1. Command(client\_id: Int, command\_id : Int, operation: String)
client can broadcast command to every server with its unique client\_id 
and command\_id, the operation will be wrote to the array in each server
our goal is to make sure that every server has the same copy of array, that is:
a. no same command will be wrote to different slot in diffent servers
b. no same slot in diffent servers will have different command 

2. B\_num(b:Int, l:Int) ordered
B\_num is used for prepare round where leader choose a integer b and different 
leaders have different id l and so that different leader with same b will have diffent value for B\_num
acceptors decide the B\_num based on this value

3.Pvalue(b: B\_num, slot : Int, c: Command)
triple

4.PvalueList
make Pvalue a list, this class simplifies the put, remove and Pmax function

5. Proposal
tuple of slot and command

6. ProposalList
critical function Xor as is presented in the paper and many search functions

7. Server
roles: 
a. replicas and acceptors

b. leader

The code can be compiled and run with no parameters, I have a simple 4 servers example, three commands broadcast to all the replicas
they all send propose request to leader

