package it.unibo.scafi.examples

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._


class PositionSensor extends AggregateProgram
  with StandardSensors with ScafiAlchemistSupport with FieldUtils {
  override def main(): Any = {
    node.put("distances", getDistances)
    node.put("directions", getDirections)
  }

  /* operation on neighbours and built-in sensors (cf., currentTime and deltaTime()) */
  def getDistances: Map[ID, D] = {
    includingSelf.reifyField(nbrRange())
  }

  def getDirections: Map[ID, P] = {
    includingSelf.reifyField(nbrVector())
  }
}
