extends KinematicBody2D

onready var gun = $gun
var bullet_speed = 2000
# TODO this is brittle - could it be attached to the scene?
var bullet = preload("res://entities/bullet.tscn")

var bullets = []
var to_remove
var current_bullet = 0
var max_bullets = 10

var fire_debounce = 0.2
var fire_cooldown = false

func _physics_process(_delta):

  if Input.is_action_pressed("ui_action"):
    if not fire_cooldown:
      fire()


func fire():
  var new_bullet = bullet.instance()
  new_bullet.position = gun.get_global_position()
  new_bullet.rotation_degrees = rotation_degrees
  new_bullet.apply_impulse(Vector2(), Vector2(bullet_speed, 0).rotated(rotation))
  get_tree().get_root().call_deferred("add_child", new_bullet)

  if bullets.size() - 1 > current_bullet:
    to_remove = bullets[current_bullet]
    if to_remove and is_instance_valid(to_remove):
      to_remove.kill()
    bullets[current_bullet] = new_bullet
  else:
    bullets.append(new_bullet)

  # increment bullet index
  current_bullet += 1
  if current_bullet > max_bullets:
    current_bullet = 0

  fire_cooldown = true
  yield(get_tree().create_timer(fire_debounce), "timeout")
  fire_cooldown = false


func kill():
  get_tree().reload_current_scene()


func _on_Area2D_body_entered(body):
  if "enemy" in body.name:
    kill()
