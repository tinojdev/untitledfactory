package main

import utils.Constants
import javafx.animation.AnimationTimer
import scalafx.application.JFXApp

import menus.StartMenu

class Ticker(function: () => Unit) extends AnimationTimer {
  override def handle(now: Long): Unit = {
    function()
  }
}

object Main extends JFXApp {

  stage = new JFXApp.PrimaryStage {
    title.value = "UntitledFactory"
    width = Constants.screenWidth
    height = Constants.screenHeight
  }
  val startMenu = new StartMenu(stage)

}
