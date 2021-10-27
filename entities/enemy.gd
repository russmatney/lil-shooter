extends KinematicBody2D

var dying = false
var dying_timer = 2


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
