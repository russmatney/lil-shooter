extends KinematicBody2D

var speed = 500
var velocity = Vector2()

var bullet_speed = 2000
var bullet = preload("res://bullet.tscn")


func _physics_process(_delta):
  var v_diff = Vector2()

  if Input.is_action_pressed("ui_right"):
    v_diff.x += 1
  if Input.is_action_pressed("ui_left"):
    v_diff.x -= 1
  if Input.is_action_pressed("ui_down"):
    v_diff.y += 1
  if Input.is_action_pressed("ui_up"):
    v_diff.y -= 1

  v_diff = v_diff.normalized() * speed
  velocity = move_and_slide(v_diff)

  look_at(get_global_mouse_position())

  if Input.is_action_pressed("ui_action"):
    fire()


func fire():
  var new_bullet = bullet.instance()
  new_bullet.position = get_global_position()
  new_bullet.rotation_degrees = rotation_degrees
  new_bullet.apply_impulse(Vector2(), Vector2(bullet_speed, 0).rotated(rotation))
  get_tree().get_root().call_deferred("add_child", new_bullet)


func kill():
  get_tree().reload_current_scene()


func _on_Area2D_body_entered(body):
  if "enemy" in body.name:
    kill()
