package at.logic.gapt.examples

import at.logic.gapt.formats.prover9.Prover9TermParserLadrStyle.parseFormula
import at.logic.gapt.proofs.Sequent
import at.logic.gapt.proofs.gaptic._

object complex {
  val decomposeLemma = Lemma(
    Sequent( Seq( "label1" -> parseFormula( "(exists x (p(x) & q(x)))" ) ), Seq( "label2" -> parseFormula( "(all y (p(y) -> (q(y) | r(y))))" ) ) )
  ) {
      decompose
    }

  val destructLemma = Lemma(
    Sequent( Nil, Seq( "label1" -> parseFormula( "a | (b | c)" ), "label2" -> parseFormula( "a & (b & c)" ) ) )
  ) {
      destruct( "label1" )
      destruct( "label2" )
    }

  val destructLemma2 = Lemma(
    Sequent( Seq( "noise1" -> parseFormula( "P(x)" ) ), Seq( "noise2" -> parseFormula( "P(y)" ), "label1" -> parseFormula( "a | (b | c)" ), "noise3" -> parseFormula( "P(z)" ), "label2" -> parseFormula( "a & (b & c)" ) ) )
  ) {
      destruct
      destruct
    }

  val chainLemma = Lemma(
    Sequent( Seq( "hyp" -> parseFormula( "(all x (q(x) -> p(f(x))))" ) ), Seq( "target" -> parseFormula( "p(f(f(c)))" ) ) )
  ) {
      chain( "hyp" ).at( "target" )
    }

  val chainLemma2 = Lemma(
    Sequent( Seq( "hyp" -> parseFormula( "(all x (p(f(x))))" ) ), Seq( "target" -> parseFormula( "p(f(f(c)))" ) ) )
  ) {
      chain( "hyp" )
    }

  val chainLemma3 = Lemma(
    Sequent( Seq( "hyp" -> parseFormula( "(all x (r(x) -> (q(x) -> p(f(x)))))" ) ), Seq( "target" -> parseFormula( "p(f(f(c)))" ) ) )
  ) {
      chain( "hyp" )
    }

  val chainLemma4 = Lemma(
    Sequent( Seq( "hyp" -> parseFormula( "(all x ((r(x) & q(x) & w(x)) -> p(f(x))))" ) ), Seq( "target" -> parseFormula( "p(f(f(c)))" ) ) )
  ) {
      chain( "hyp" )
    }
}
