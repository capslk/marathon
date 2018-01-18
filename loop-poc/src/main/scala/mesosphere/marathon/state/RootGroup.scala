package mesosphere.marathon.state

import scala.collection.immutable.TreeMap
import monocle.function.At.at
import monocle.std.map._
import monocle.macros.GenLens
import monocle.function.all._

case class RootGroup(apps: Map[Path, Map[Variant, RunSpec]]) {
  import RootGroup._
  val appsLens = GenLens[RootGroup](_.apps)

  def get(ref: RunSpecRef): Option[RunSpec] =
    for {
      variants <- apps.get(ref.id)
      runSpec <- variants.get(ref.variant)
    } yield runSpec

  def withoutApp(ref: RunSpecRef): RootGroup = {
    val runSpecVariants =
      apps.getOrElse(ref.id, Map.empty) - ref.variant

    appsLens.
      composeLens(at(ref.id)).
      set(if (runSpecVariants.isEmpty) None else Some(runSpecVariants)).
      apply(this)
  }

  def withApp(runSpec: RunSpec): RootGroup = {
    val runSpecVariants =
      apps.getOrElse(runSpec.id, Map.empty).updated(runSpec.variant, runSpec)
    appsLens.
      composeLens(at(runSpec.id)).
      set(Some(runSpecVariants)).
      apply(this)
  }
}

object RootGroup {
  val empty: RootGroup = RootGroup(apps = Map.empty)
}