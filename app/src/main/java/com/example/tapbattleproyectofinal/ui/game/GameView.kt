package com.example.tapbattleproyectofinal.ui.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.tapbattleproyectofinal.models.Target
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

//Vista del canvas del juego completo
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Target actual
    private var currentTarget: Target? = null

    // Callback cuando se toca un objetivo
    var onTargetHit: ((Target) -> Unit)? = null

    // Partículas para efectos visuales
    private val particles = mutableListOf<Particle>()

    // Paints para dibujar
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val targetBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.WHITE
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Animador para el pulso del objetivo
    private var pulseAnimator: ValueAnimator? = null
    private var pulseScale = 1f

    // Colores del degradado
    private val colors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"),
        Color.parseColor("#FFA07A"),
        Color.parseColor("#98D8C8")
    )

    init {
        // Iniciar animación de partículas
        startParticleAnimation()
    }

    // Establece un nuevo objetivo
    fun setTarget(target: Target?) {
        this.currentTarget = target

        if (target != null) {
            startPulseAnimation()
        } else {
            stopPulseAnimation()
        }

        invalidate()
    }

    // Animación de pulso para el objetivo
    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                pulseScale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseScale = 1f
    }

    // Dibuja el objetivo y efectos
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar partículas de fondo
        drawParticles(canvas)

        // Dibujar objetivo actual
        currentTarget?.let { target ->
            if (!target.isExpired()) {
                drawTarget(canvas, target)
            }
        }
    }

    // Dibuja el objetivo con gradiente y efectos
    private fun drawTarget(canvas: Canvas, target: Target) {
        val progress = target.getLifeProgress()
        val radius = target.r * pulseScale

        // Color basado en el progreso de vida
        val colorIndex = (progress * (colors.size - 1)).toInt().coerceIn(0, colors.size - 1)
        val targetColor = colors[colorIndex]

        // Gradiente radial
        val shader = RadialGradient(
            target.cx, target.cy, radius,
            intArrayOf(targetColor, Color.TRANSPARENT),
            floatArrayOf(0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        targetPaint.shader = shader

        // Dibujar círculo principal
        canvas.drawCircle(target.cx, target.cy, radius, targetPaint)

        // Dibujar borde
        canvas.drawCircle(target.cx, target.cy, radius, targetBorderPaint)

        // Dibujar progreso de vida (círculo que se va llenando)
        progressPaint.color = Color.WHITE
        val sweepAngle = 360f * (1f - progress)
        canvas.drawArc(
            target.cx - radius - 20f,
            target.cy - radius - 20f,
            target.cx + radius + 20f,
            target.cy + radius + 20f,
            -90f,
            sweepAngle,
            false,
            progressPaint
        )
    }

    //Dibuja las particulas
    private fun drawParticles(canvas: Canvas) {
        particles.forEach { particle ->
            particlePaint.color = particle.color
            particlePaint.alpha = (particle.alpha * 255).toInt()
            canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint)
        }
    }

    // Efecto de explosión cuando se toca un objetivo
    fun createExplosion(x: Float, y: Float, color: Int) {
        for (i in 0 until 20) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val speed = Random.nextFloat() * 10f + 5f
            val size = Random.nextFloat() * 8f + 4f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    size = size,
                    color = color
                )
            )
        }
    }

    //Animación en el circulo que aparece
    private fun startParticleAnimation() {
        post(object : Runnable {
            override fun run() {
                updateParticles()
                invalidate()
                postDelayed(this, 16) // ~60 FPS
            }
        })
    }

    //Actualiza los circulos que aparecen
    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update()

            // Eliminan los circulos que ya expiraron
            if (particle.isDead() ||
                particle.x < 0 || particle.x > width ||
                particle.y < 0 || particle.y > height) {
                iterator.remove()
            }
        }
    }

    // Maneja toques en la pantalla
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val target = currentTarget
            if (target != null && !target.isExpired()) {
                if (target.containsPoint(event.x, event.y)) {
                    // Hit exitoso
                    val colorIndex = Random.nextInt(colors.size)
                    createExplosion(target.cx, target.cy, colors[colorIndex])
                    onTargetHit?.invoke(target)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // Limpia recursos
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPulseAnimation()
        particles.clear()
    }
}

//Clase para partículas del efecto visual
private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Int,
    var alpha: Float = 1f,
    var life: Float = 1f
) {
    fun update() {
        x += vx
        y += vy
        vy += 0.3f // Gravedad
        life -= 0.02f
        alpha = life.coerceIn(0f, 1f)
        size *= 0.98f
    }

    fun isDead(): Boolean = life <= 0f || size < 0.5f
}