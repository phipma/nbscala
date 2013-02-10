package org.netbeans.modules.scala.sbt.project

import java.beans.PropertyChangeListener
import javax.swing.Icon
import javax.swing.ImageIcon
import org.netbeans.api.project.Project
import org.netbeans.api.project.ProjectInformation
import org.netbeans.api.project.ProjectManager
import org.netbeans.modules.scala.sbt.classpath.SBTClassPathProvider
import org.netbeans.modules.scala.sbt.classpath.SBTResolver
import org.netbeans.modules.scala.sbt.classpath.SBTSources
import org.netbeans.spi.project.ProjectState
import org.openide.filesystems.FileObject
import org.openide.util.ImageUtilities
import org.openide.util.Lookup
import org.openide.util.lookup.Lookups

/**
 * 
 * @author Caoyuan Deng
 */
class SBTProject(projectDir: FileObject, state: ProjectState) extends Project {
  private lazy val lookup: Lookup = Lookups.fixed(
    this,
    new Info(),
    new SBTResolver(this, true),
    new SBTProjectLogicalView(this),
    new SBTClassPathProvider(this),
    new SBTSources(this),
    new SBTProjectOpenedHook(this),
    new SBTActionProvider(this),
    new SBTDepProjectProvider(this)
  )

  override
  def getProjectDirectory = projectDir

  override
  def getLookup: Lookup = lookup
  
  def getMasterProject: Option[SBTProject] = {
    projectDir.getParent match {
      case parentDir: FileObject if parentDir.isFolder =>
        parentDir.getFileObject(ProjectConstants.PROJECT_FOLDER_NAME) match {
          case projectFolder: FileObject if projectFolder.isFolder =>
            ProjectManager.getDefault.findProject(parentDir) match {
              case x: SBTProject => Some(x)
              case _ => None
            }
          case _ => None
        }
      case _ => None
    }
  }
  
  /**
   * Top parent project or myself
   */
  def getRootProject: SBTProject = {
    getRootProject(this, this)
  }
  
  private def getRootProject(project: SBTProject, lastFound: SBTProject): SBTProject = {
    project.getMasterProject match {
      case None => lastFound
      case Some(x) => getRootProject(x, x)
    }
  }
  
  private final class Info extends ProjectInformation {

    override
    def getIcon: Icon = new ImageIcon(SBTProject.SBT_ICON)

    override
    def getName: String = {
      getProjectDirectory.getName
    }

    override
    def getDisplayName: String = {
      getName
    }

    override
    def addPropertyChangeListener(pcl: PropertyChangeListener) {
      //do nothing, won't change
    }

    override
    def removePropertyChangeListener(pcl: PropertyChangeListener) {
      //do nothing, won't change
    }

    override
    def getProject: Project = SBTProject.this
  }
  
}

object SBTProject {
  lazy val SBT_ICON = ImageUtilities.loadImage("org/netbeans/modules/scala/sbt/resources/sbt.png")
}