package at.logic.gapt.provers.prover9

import at.logic.gapt.expr._
import at.logic.gapt.expr.hol.univclosure
import at.logic.gapt.proofs.resolution.inputClauses
import at.logic.gapt.proofs.{ Sequent, HOLSequent, HOLClause }
import at.logic.gapt.proofs.lk.base.RichOccSequent
import at.logic.gapt.formats.prover9.Prover9TermParserLadrStyle.parseFormula

import org.specs2.mutable._

import scala.io.Source

class Prover9Test extends Specification {
  val prover9 = new Prover9Prover

  args( skipAll = !new Prover9Prover().isInstalled )
  "The Prover9 interface" should {
    "prove identity" in {
      val k = FOLConst( "k" )
      val s = HOLSequent( Seq(), Seq( Eq( k, k ) ) )
      prover9.getLKProof( s ) must beLike {
        case Some( p ) => p.root.toHOLSequent must_== s
      }
    }

    "prove { A or B :- -(-A and -B)  }" in {
      val Seq( a, b ) = Seq( "A", "B" ).map( FOLAtom( _ ) )
      val s = HOLSequent( Seq( Or( a, b ) ), Seq( Neg( And( Neg( a ), Neg( b ) ) ) ) )
      prover9.getLKProof( s ) must beLike {
        case Some( p ) => p.root.toHOLSequent must_== s
      }
    }

    "handle quantified antecedents" in {
      val seq = HOLSequent(
        Seq( "0+x=x", "s(x)+y=s(x+y)" ).map( s => univclosure( parseFormula( s ) ) ),
        Seq( parseFormula( "s(0)+s(s(0)) = s(s(s(0)))" ) )
      )
      prover9.getLKProof( seq ) must beLike {
        case Some( p ) => p.root.toHOLSequent must_== seq
      }
    }

    "prove top" in { prover9.getLKProof( HOLSequent( Seq(), Seq( Top() ) ) ) must beSome }
    "not prove bottom" in { prover9.getLKProof( HOLSequent( Seq(), Seq( Bottom() ) ) ) must beNone }
    "not refute top" in { prover9.getLKProof( HOLSequent( Seq( Top() ), Seq() ) ) must beNone }
    "refute bottom" in { prover9.getLKProof( HOLSequent( Seq( Bottom() ), Seq() ) ) must beSome }

    "ground sequents" in {
      val seq = HOLSequent( Seq( parseFormula( "x=y" ) ), Seq( parseFormula( "y=x" ) ) )
      prover9.getLKProof( seq ) must beLike {
        case Some( p ) => p.root.toHOLSequent must_== seq
      }
    }

    "treat variables in sequents as constants" in {
      val seq = "P(x)" +: Sequent() :+ "P(c)" map parseFormula
      prover9.getExpansionSequent( seq ) must beNone
    }

    "handle exit code 2" in {
      val cnf = List( HOLClause( Seq(), Seq() ), HOLClause( Seq( FOLAtom( "a" ) ), Seq() ) )
      prover9.getRobinsonProof( cnf ) must beLike {
        case Some( p ) => inputClauses( p ) must contain( atMost( cnf.toSet ) )
      }
    }
  }

  "Prover9 proof output loader" should {
    def load( fn: String ) = Source.fromInputStream( getClass.getClassLoader.getResourceAsStream( fn ) ).mkString

    "goat puzzle PUZ047+1.out" in {
      Prover9Importer.lkProof( load( "PUZ047+1.out" ) )
      ok
    }

    "expansion proof paper example cade13example.out" in {
      Prover9Importer.lkProof( load( "cade13example.out" ) )
      ok
    }

    "proof with new_symbol" in {
      Prover9Importer.lkProof( load( "ALG138+1.out" ) )
      ok
    }

    "other proof with new_symbol" in {
      Prover9Importer.expansionProof( load( "SEU034+1.out" ) )
      ok
    }

    "strong quantifiers" in {
      Prover9Importer.lkProof( load( "GEO200+1.out" ) )
      ok
    }

    "cnf with different equation order" in {
      Prover9Importer.lkProof( load( "NUM561+2.out" ) )
      ok
    }
  }

}
