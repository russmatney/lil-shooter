extends KinematicBody2D


var motion = Vector2()
var player


func _ready():
  player = get_parent().get_node("player")


func _physics_process(delta):
  position += (player.position - position) / 50

  look_at(player.position)

  move_and_collide(motion)


func kill():
  queue_free()


func _on_Area2D_body_entered(body):
  # TODO walk the rainbow
  if "bullet" in body.name:
    kill()
