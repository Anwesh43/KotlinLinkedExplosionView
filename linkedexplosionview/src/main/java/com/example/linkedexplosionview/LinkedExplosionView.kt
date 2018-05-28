package com.example.linkedexplosionview

/**
 * Created by anweshmishra on 29/05/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.*

class LinkedExplosionView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var dir : Float = 0f, var scale : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * this.dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }

    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate (updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LEBNode (val cbs : ArrayList<(Canvas, Paint, Float) -> Unit>, val state : State = State()) {

        var cb : (Canvas, Paint, Float) -> Unit = cbs[0]

        var next : LEBNode? = null

        var prev : LEBNode? = null

        init {
            cbs.removeAt(0)
            if (cbs.size > 0) {
                this.addNeighbor()
            }
        }

        fun addNeighbor() {
            next = LEBNode(cbs)
            next?.prev = this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            cb.invoke(canvas, paint, state.scale)
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LEBNode {
            var curr : LEBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedExplosion(var i : Int) {

        var dir : Int = 1

        var curr : LEBNode? = null

        init {
            val cbs : ArrayList<(Canvas, Paint, Float) -> Unit> = ArrayList()
            cbs.add {canvas, paint, fl ->
                paint.alpha = 255
                paint.color = Color.RED
                val w : Float = canvas.width.toFloat()
                val h : Float = canvas.height.toFloat()
                val r : Float = Math.min(w, h) / 20
                canvas.drawCircle(w - (w/2) * fl,(h/2) * fl, r, paint)
            }
            cbs.add { canvas, paint, fl ->
                paint.alpha = 255
                paint.color = Color.RED
                val w : Float = canvas.width.toFloat()
                val h : Float = canvas.height.toFloat()
                val r : Float = Math.min(w, h) / 20
                canvas.drawCircle(w/2, h/2, r * (1 - fl), paint)
                val size : Float = Math.min(w, h) / 3
                paint.alpha = (255 * (1 - fl)).toInt()
                for (i in 1..6) {
                    canvas.save()
                    canvas.translate(w/2, h/2)
                    canvas.rotate(60f * i)
                    canvas.drawCircle(fl * size, 0f, size, paint)
                    canvas.restore()
                }
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            curr?.update {scale ->
                curr = curr?.getNext(dir) {
                    dir *= -1
                    stopcb(scale)
                }
                stopcb(scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr?.startUpdating(startcb)
        }

        fun draw(canvas: Canvas, paint : Paint) {
            curr?.draw(canvas, paint)
        }

    }

    data class Renderer(var view : LinkedExplosionView) {

        val animator : Animator = Animator(view)

        val linkedExplosion : LinkedExplosion = LinkedExplosion(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedExplosion.draw(canvas, paint)
            animator.animate {
                linkedExplosion.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedExplosion.startUpdating {
                animator.start()
            }
        }
    }
}