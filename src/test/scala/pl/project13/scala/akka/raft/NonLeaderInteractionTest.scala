package pl.project13.scala.akka.raft

import pl.project13.scala.akka.raft.protocol._
import concurrent.duration._
import akka.testkit.TestProbe
import pl.project13.scala.akka.raft.example.protocol._
import org.scalatest.concurrent.Eventually
import org.scalatest.GivenWhenThen

class NonLeaderInteractionTest extends RaftSpec(callingThreadDispatcher = false)
  with GivenWhenThen
  with Eventually {

  behavior of "Non Leader Interaction"

  val initialMembers = 5

  val timeout = 2.second

  val client = TestProbe()

  it should "allow contacting a non-leader member, which should respond with the Leader's ref" in {
    Given("A leader is elected")
    subscribeElectedLeader()
    awaitElectedLeader()
    infoMemberStates()

    val msg = ClientMessage(client.ref, AppendWord("test"))

    When("The client sends a write message to a non-leader member")
    followers().head ! msg

    Then("That non-leader, should respons with the leader's ref")
    val leaderIs = expectMsgType[LeaderIs](max = 1.second)

    When("The client contact that member")
    leaderIs.ref.get ! msg

    Then("The leader should take the write")
    client.expectMsg(timeout, "test")
  }

}
