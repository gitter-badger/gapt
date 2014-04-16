/** 
 * Description: 
**/

package at.logic.transformations.ceres.clauseSets

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.xml._
import scala.io._
import java.io.File.separator
import at.logic.language.hol._
import at.logic.language.lambda.symbols.ImplicitConverters._
import at.logic.calculi.occurrences._
import at.logic.transformations.ceres.struct._
import java.io.{FileInputStream, InputStreamReader}
import at.logic.algorithms.shlk._
import at.logic.calculi.slk.SchemaProofDB
import at.logic.algorithms.lk.{getAncestors, getCutAncestors}
import at.logic.language.lambda.symbols.VariableStringSymbol
import at.logic.calculi.lk.base.Sequent
import at.logic.transformations.ceres.projections.{DeleteTautology, DeleteRedundantSequents}
import at.logic.language.lambda.typedLambdaCalculus.Var
import at.logic.language.schema.Pred._
import at.logic.transformations.ceres.clauseSchema.SchemaSubstitution3
import at.logic.language.schema._

@RunWith(classOf[JUnitRunner])
class ClauseSetsTest extends SpecificationWithJUnit {

  // commented out --- we need to construct formula occurrences, but they
  //   are abstract and the factory is private to LK
  sequential
  "ClauseSets" should {
    "- transform a Struct into a standard clause set" in {
//      val a = HOLVarFormula( "a" )
//      val b = HOLVarFormula( "b" )
//      val c = HOLVarFormula( "c" )
//      val d = HOLVarFormula( "d" )
//
//      val struct = Times(Plus(A(a), A(b)), Plus(A(c), A(d)))
//      val cs = StandardClauseSet.transformStructToClauseSet( struct ).getSequent
//      cs must verify( clauses => clauses.forall( seq => seq.multisetEquals( Sequent( Nil, a::c::Nil ) ) ||
//                                 seq.multisetEquals( Sequent( Nil, a::d::Nil ) ) ||
//                                 seq.multisetEquals( Sequent( Nil, b::c::Nil ) ) ||
//                                 seq.multisetEquals( Sequent( Nil, b::d::Nil ) ) ) )
      ok
    }

    "test the schematic struct in journal_example.slk" in {
      //println(Console.GREEN+"\n\n-------- schematic struct in journal_example.slk --------\n\n"+Console.RESET)
      val s = new InputStreamReader(new FileInputStream("target" + separator + "test-classes" + separator + "journal_example.lks"))
      val map = sFOParser.parseProof(s)
      val p = map.get("\\psi").get._2.get("root").get
      //println(StructCreators.extract(p))
//      val str =
      //println("\n\n\n\n")
      ok
    }


    "test the schematic struct in sEXP.slk" in {
      //println(Console.BLUE+"\n\n---- schematic struct in sEXP.slk ----\n\n\n"+Console.RESET)
      val s = new InputStreamReader(new FileInputStream("target" + separator + "test-classes" + separator + "sEXP.lks"))
      SchemaProofDB.clear
      val map = sFOParser.parseProof(s)
      val p1s = map.get("\\psi").get._2.get("root").get
      val p2s = map.get("\\varphi").get._2.get("root").get
      val p1b = map.get("\\psi").get._1.get("root").get
      val p2b = map.get("\\varphi").get._1.get("root").get
      //println(StructCreators.extract(p1s, getCutAncestors(p1s)))
      //println("\n\n")
      //println(StructCreators.extract(p2s, getCutAncestors(p2s) ++ getAncestors(p2s.root.succedent.head)))
      //println("\n\n")
      //println(StructCreators.extract(p1b, Set.empty[FormulaOccurrence]))
      //println("\n\n")
      //println(StructCreators.extract(p2b, getAncestors(p2b.root.succedent.head)))
      //println("\n\n")
//      println("rename cl symbols to CL_i(k): ")
      val n = IntVar(new VariableStringSymbol("n"))
//      val struct = StructCreators.extractStruct( "\\psi", n)
      val struct = StructCreators.extract(p1s, getCutAncestors(p1s))
      val cs : List[Sequent] = DeleteRedundantSequents( DeleteTautology( StandardClauseSet.transformStructToClauseSet(struct) ))
      val pair = renameCLsymbols(cs)
//      println("map:\n" + pair._2)
//      println("\n\n\n\n")
//      println("rename:\n" + pair._1)
      val new_map = Map.empty[Var, IntegerTerm] + Pair(IntVar(new VariableStringSymbol("k")).asInstanceOf[Var], Succ(IntZero()) )
      var subst = new SchemaSubstitution3(new_map)
      val gr = groundStruct(struct, subst)
      //println("\nground struct : \n"+gr)
      val unfold_gr = unfoldGroundStruct(gr)
      //println("\nunfold ground struct : \n"+unfold_gr)

      val cs_gr = StandardClauseSet.transformStructToClauseSet(gr)
//      println("\nground struct : \n"+cs_gr)
      //println("\n\n\n\n")
      ok
    }
  }

}