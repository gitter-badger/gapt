package at.logic.gapt.examples.tip.isaplanner

import better.files._
import at.logic.gapt.expr._
import at.logic.gapt.formats.tip.TipSmtParser
import at.logic.gapt.proofs.Context.{ InductiveType, Sort }
import at.logic.gapt.proofs.{ Ant, Sequent }
import at.logic.gapt.proofs.gaptic._

object isaplanner40 extends TacticsProof {
  val bench = TipSmtParser.fixupAndParse( file"examples/tip/isaplanner/prop_40.smt2" )
  ctx = bench.ctx

  val sequent = bench.toSequent.zipWithIndex.map {
    case ( f, Ant( i ) ) => s"h$i" -> f
    case ( f, _ )        => "goal" -> f
  }

  val proof = Lemma( sequent ) {
    trivial
  }
}
