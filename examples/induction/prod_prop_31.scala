package at.logic.gapt.examples.induction
import at.logic.gapt.examples.Script
import at.logic.gapt.expr.hol._
import at.logic.gapt.expr._
import at.logic.gapt.formats.tip.TipSmtParser
import at.logic.gapt.grammars._
import at.logic.gapt.proofs.Sequent
import at.logic.gapt.proofs.expansion.{ InstanceTermEncoding, extractInstances }
import at.logic.gapt.proofs.lk.{ ExtractInterpolant, LKToExpansionProof, skolemize }
import at.logic.gapt.proofs.reduction._
import at.logic.gapt.proofs.resolution.RobinsonToExpansionProof
import at.logic.gapt.provers.escargot.Escargot
import at.logic.gapt.provers.inductionProver.{ hSolveQBUP, qbupForRecSchem, simplePi1RecSchemTempl }
import at.logic.gapt.provers.smtlib.Z3
import at.logic.gapt.provers.spass.SPASS

object prod_prop_31 extends Script {
  val tipProblem = TipSmtParser parse
    """
  (declare-sort sk_a 0)
  (declare-datatypes ()
    ((list (nil) (cons (head sk_a) (tail list)))))
  (define-fun-rec
    qrev
      ((x list) (y list)) list
      (match x
        (case nil y)
        (case (cons z xs) (qrev xs (cons z y)))))
  (assert-not (forall ((x list)) (= (qrev (qrev x nil) nil) x)))
  (check-sat)
"""
  implicit var ctx = tipProblem.context

  val sequent = tipProblem toSequent

  val list = TBase( "list" )
  val sk_a = TBase( "sk_a" )
  val nil = le"nil"
  val cons = le"cons"

  val as = 0 until 10 map { j => Const( s"a$j", sk_a ) }
  ctx ++= as

  def mkList( i: Int ) = ( 0 until i ).foldRight[LambdaExpression]( nil ) { ( j, l ) => cons( as( j ), l ) }

  val instances = 0 to 2 map mkList

  // Compute many-sorted expansion sequents
  val instanceProofs = instances map { inst =>
    val instanceSequent = sequent.map( identity, instantiate( _, inst ) )
    if ( true ) {
      if ( true ) {
        val reduction = CNFReductionExpRes |> PredicateReductionCNF |> ErasureReductionCNF
        val ( erasedCNF, back ) = reduction forward instanceSequent
        val Some( erasedProof ) = SPASS getRobinsonProof erasedCNF
        val reifiedExpansion = back( erasedProof )
        inst -> reifiedExpansion
      } else {
        val reduction = PredicateReductionET |> ErasureReductionET
        val ( erasedInstanceSequent, back ) = reduction forward instanceSequent
        val Some( erasedExpansion ) = SPASS getExpansionProof erasedInstanceSequent
        val reifiedExpansion = back( erasedExpansion )
        require( Z3 isValid reifiedExpansion.deep )
        inst -> reifiedExpansion
      }
    } else {
      inst -> Escargot.getExpansionProof( instanceSequent ).get
    }
  }

  instanceProofs foreach {
    case ( inst, es ) =>
      println( s"Instances for x = $inst:" )
      extractInstances( es ).map( -_, identity ).elements foreach println
      println()
  }

  val x = Var( "x", list )
  val encoding = InstanceTermEncoding( sequent.map( identity, instantiate( _, x ) ) )

  val A = Const( "A", list -> encoding.instanceTermType )
  val w = Var( "w", list )

  val template = simplePi1RecSchemTempl( A( x ), Seq( list ) )
  println( "Recursion scheme template:" )
  template.template.toSeq.sortBy { _._1.toString } foreach println
  println()

  val targets = for ( ( inst, es ) <- instanceProofs; term <- encoding encode es ) yield A( inst ) -> term
  val stableRS = template.stableRecSchem( targets.toSet )
  // FIXME: the class of rs w/o skolem symbols is not closed under the rewriting that stableTerms() expects :-/
  val stableRSWithoutSkolemSymbols =
    RecursionScheme( stableRS.axiom, stableRS.nonTerminals,
      stableRS.rules filterNot {
        case Rule( from, to ) =>
          constants( to ) exists { _.exptype == sk_a }
      } )
  //println(stableRSWithoutSkolemSymbols)
  val rs = minimizeRecursionScheme( stableRSWithoutSkolemSymbols, targets, template.targetFilter,
    weight = rule => expressionSize( rule.lhs === rule.rhs ) )
  println( s"Minimized recursion scheme:\n$rs\n" )

  val logicalRS = encoding decode rs
  println( s"Logical recursion scheme:\n$logicalRS\n" )

  val inst = mkList( 8 )
  val lang = logicalRS parametricLanguage inst map { _.asInstanceOf[HOLFormula] }
  println( s"Validity for instance x = ${inst.toSigRelativeString}:" )
  println( Z3 isValid Or( lang toSeq ) )
  if ( true ) {
    val Some( lk ) = Escargot getLKProof Sequent( lang.toSeq map { case Neg( f ) => ( f, false ) case f => ( f, true ) } )
    println( "Interpolant of the instance sequent:" )
    println( simplify( ExtractInterpolant( lk, lk.conclusion.map( _ => false, _ => true ) )._3 ).toSigRelativeString )
  }
  println()

  val qbup @ Ex( x_G, qbupMatrix ) = qbupForRecSchem( logicalRS )
  println( s"QBUP:\n${qbup.toSigRelativeString}\n" )

  println( s"Canonical solution at G(${mkList( 3 )},w):" )
  val G_ = logicalRS.nonTerminals.find( _.name == "G" ).get
  val canSol = And( logicalRS generatedTerms G_( mkList( 3 ), w ) map { -_ } )
  CNFp.toClauseList( canSol ).map { _.map { _.toSigRelativeString } } foreach println
  println()

  val Some( solution ) = hSolveQBUP( qbupMatrix, x_G( mkList( 3 ), w ), canSol )
  println()

  val formula = BetaReduction.betaNormalize( instantiate( qbup, solution ) )
  println( s"Solution: ${solution.toSigRelativeString}\n" )
  println( Z3 isValid skolemize( formula ) )
}
