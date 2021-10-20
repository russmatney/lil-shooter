extends KinematicBody2D


func kill():
  get_tree().reload_current_scene()


func _on_Area2D_body_entered(body):
  if "enemy" in body.name:
    kill()
