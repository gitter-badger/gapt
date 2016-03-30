package at.logic.gapt.proofs.epsilon

import at.logic.gapt.algorithms.rewriting.TermReplacement
import at.logic.gapt.expr._
import at.logic.gapt.expr.hol.instantiate
import at.logic.gapt.proofs.expansion._

object ExpansionProofToEpsilon {

  def apply( e: ExpansionProof ): EpsilonProof = {
    val skolemToEpsilonMap = e.skolemFunctions.skolemDefs.map {
      case ( sk, Abs.Block( vs, q @ Quant( x, _ ) ) ) =>
        val x_ = rename( x, vs )
        ( sk: LambdaExpression ) -> Abs.Block( vs, Epsilon( x_, epsilonize( q match {
          case All( _, _ ) => -instantiate( q, x_ )
          case Ex( _, _ )  => instantiate( q, x_ )
        } ) ) )
    }
    def replaceSkolemByEpsilon( t: LambdaExpression ) =
      BetaReduction.betaNormalize( TermReplacement( t, skolemToEpsilonMap ) )

    val criticalFormulas = e.subProofs flatMap {
      case ETWeakQuantifier( sh, insts ) =>
        val ex = sh match {
          case All( x, f ) => Ex( x, -epsilonize( replaceSkolemByEpsilon( f ).asInstanceOf[HOLFormula] ) )
          case Ex( x, f )  => Ex( x, epsilonize( replaceSkolemByEpsilon( f ).asInstanceOf[HOLFormula] ) )
        }
        for ( ( term, _ ) <- insts ) yield CriticalFormula( ex, replaceSkolemByEpsilon( term ) )
      case ETStrongQuantifier( _, _, _ ) =>
        throw new IllegalArgumentException
      case _ => Set()
    }

    EpsilonProof( criticalFormulas.toSeq, e.shallow )
  }

}
