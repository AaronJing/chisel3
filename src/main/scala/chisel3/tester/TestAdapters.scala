// See LICENSE for license details.

package chisel3.tester

import chisel3._
import chisel3.util._

// TODO get rid of this boilerplate
import chisel3.internal.firrtl.{LitArg, ULit, SLit}

package object TestAdapters {
  // TODO: clock should be optional
  class ReadyValidSource[T <: Data](x: ReadyValidIO[T], clk: Clock) {
    // TODO assumption this never goes out of scope
    x.valid.weakPoke(false.B)

    def enqueueNow(data: T): Unit = {
      fork { timescope {
        x.ready.expect(true.B)
        x.bits.poke(data)
        x.valid.poke(true.B)
        clk.step(1)
      }}
    }

    def enqueueSeq(data: Seq[T]): TesterThreadList = {
      fork {
        for (elt <- data) {
          while (x.ready.peek().litToBoolean == false) {
            clk.step(1)
          }
          timescope {
            x.bits.poke(elt)
            x.valid.poke(true.B)
            clk.step(1)
          }
        }
      }
    }
  }

  class ReadyValidSink[T <: Data](x: ReadyValidIO[T], clk: Clock) {
    // TODO assumption this never goes out of scope
    x.ready.weakPoke(false.B)

    def expectDequeueNow(data: T): Unit = {
      fork { timescope {
        x.valid.expect(true.B)
        x.bits.expect(data)
        x.ready.poke(true.B)
        clk.step(1)
      } }
    }

    def expectPeekNow(data: T): Unit = {
      x.valid.expect(true.B)
      x.bits.expect(data)
    }

    def expectInvalid(): Unit = {
      x.valid.expect(false.B)
    }
  }
}
