package it.unibo.scafi.examples

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import sttp.client3.{Response, _}



class DroneControl extends AggregateProgram
  with StandardSensors with ScafiAlchemistSupport with BlockG with Gradients with FieldUtils {
  override def main(): Any = {
    val port = sense[Int]("port")
    var charge = sense[Double]("charge")
    val own_position = currentPosition()

    if (own_position.x == 0 && own_position.y == 0) {
      if (charge < 100) {charge += 0.2}
      else {charge = 100}
    } else if (charge > 5) {
      charge -= 0.05
    }
    node.put("charge", charge)
    var distances: Map[ID, D] = Map.empty
    var directions: Map[ID, P] = Map.empty
    try {
      distances = sense[Map[ID, D]]("distances") // Alchemist API => node.get("test")
      directions = sense[Map[ID, P]]("directions")
    } catch {
      case e: Exception => println(e)
    }

    val distances_obj = ujson.Obj()
    for ((key, value) <- distances) {
      distances_obj.update(key.toString, value)
    }
    val directions_obj = ujson.Obj()
    for ((key, value) <- directions) {
      directions_obj.update(key.toString, ujson.Arr(value.x, value.y))
    }

    quickRequest
      .get(uri"http://localhost:${port}/update/${ujson.write(distances_obj)}/${ujson.write(directions_obj)}/${own_position.x},${own_position.y}/$charge")
      .send(HttpClientSyncBackend())

    val response2: Response[String] = quickRequest
      .get(uri"http://localhost:${port}/get")
      .send(HttpClientSyncBackend())
    val json_content = ujson.read(response2.body).obj
    val target_array = json_content.get("target").get.arr
    val target: (Double, Double) = (target_array(0).num, target_array(1).num)
    node.put("target", target)
  }
}
