package paxutil

class ActorBag(params : List[ActorData]) {
    val actors = params.map(ad => (ad.id, ad.makeActorHandle))

    def getActBySym(id : Symbol) = actors.find(_._1 == id).map(_._2)

    def actorsToList() = actors.unzip._2

    def symbolsToList() = actors.unzip._1
}
