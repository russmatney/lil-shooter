extends KinematicBody2D


# var motion = Vector2()
# var player

var dying = false
var dying_timer = 2

# onready var sprite = $Sprite
# var index = 0


func _ready():
  pass
  # player = get_parent().get_node("player")


func _physics_process(delta):
  pass
  # if dying:
  #   set_scale(Vector2(scale.x/1.03, scale.y/1.04))
  # else:
  #   move(delta)

# func move(delta):
#   position += (player.position - position) / 50

#   look_at(player.position)

#   move_and_collide(motion)

func hit():
  pass
  # health -= 1
  # if health < 1:
  #   kill()
  #   return

  # index = (index + 1) % colors.size()
  # sprite.modulate = colors[index]

func kill():
  dying = true
  yield(get_tree().create_timer(dying_timer), "timeout")
  dying = false
  queue_free()


func _on_Area2D_body_entered(body):
  if "bullet" in body.name:
    hit()
    if is_instance_valid(body):
      print("killing bullet .gd (from enemy)", body)
      body.queue_free()
