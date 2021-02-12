package wrenchez

import java.time.LocalDate

import subatomic._
import subatomic.builders._
import subatomic.builders.librarysite._

object Build extends LibrarySite.App {
  override def extra(site: Site[LibrarySite.Doc]) = {
    site
      .addCopyOf(SiteRoot / "CNAME", os.pwd / "docs" / "assets" / "CNAME")
  }

  val currentYear = LocalDate.now().getYear()

  def config = LibrarySite(
    contentRoot = os.pwd / "docs" / "pages",
    name = "Wrenchez",
    tagline = Some("A weaver integration for Natchez"),
    githubUrl = Some("https://github.com/indoorvivants/weaver-natchez"),
    assetsFilter = _.baseName != "CNAME",
    copyright = Some(s"© 2021-$currentYear Anton Sviridov"),
    assetsRoot = Some(os.pwd / "docs" / "assets"),
    highlightJS = HighlightJS.default.copy(theme = "monokai-sublime")
  )
}

