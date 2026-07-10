<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script setup>
defineOptions({ name: 'ParticleBackground' })
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null
let particles = []
let mouse = { x: null, y: null, radius: 150 }
let resizeHandler = null
let mouseMoveHandler = null
let mouseLeaveHandler = null

// 粒子配置
const config = {
  particleCount: 80,
  connectionDistance: 120,
  mouseDistance: 150,
  baseSpeed: 0.5,
  colors: {
    primary: 'rgba(88, 166, 255, 0.5)', // 电光蓝
    secondary: 'rgba(63, 185, 80, 0.4)', // 荧光绿
    accent: 'rgba(210, 153, 34, 0.3)' // 琥珀黄
  }
}

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.size = Math.random() * 2 + 1
    this.speedX = (Math.random() - 0.5) * config.baseSpeed
    this.speedY = (Math.random() - 0.5) * config.baseSpeed
    this.color = this.getRandomColor()
  }

  getRandomColor() {
    const colors = Object.values(config.colors)
    return colors[Math.floor(Math.random() * colors.length)]
  }

  update() {
    // 边界检测
    if (this.x > this.canvas.width || this.x < 0) {
      this.speedX = -this.speedX
    }
    if (this.y > this.canvas.height || this.y < 0) {
      this.speedY = -this.speedY
    }

    // 鼠标交互 - 粒子逃离鼠标
    if (mouse.x != null && mouse.y != null) {
      let dx = mouse.x - this.x
      let dy = mouse.y - this.y
      let distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < config.mouseDistance) {
        const forceDirectionX = dx / distance
        const forceDirectionY = dy / distance
        const force = (config.mouseDistance - distance) / config.mouseDistance
        const directionX = forceDirectionX * force * 2
        const directionY = forceDirectionY * force * 2

        this.x -= directionX
        this.y -= directionY
      }
    }

    this.x += this.speedX
    this.y += this.speedY
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = this.color
    ctx.fill()
  }
}

function initParticles() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')

  // 设置画布尺寸
  resizeHandler = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resizeHandler()
  window.addEventListener('resize', resizeHandler)

  // 创建粒子
  particles = []
  for (let i = 0; i < config.particleCount; i++) {
    particles.push(new Particle(canvas))
  }

  // 鼠标事件监听
  mouseMoveHandler = (e) => {
    mouse.x = e.x
    mouse.y = e.y
  }
  mouseLeaveHandler = () => {
    mouse.x = null
    mouse.y = null
  }
  window.addEventListener('mousemove', mouseMoveHandler)
  window.addEventListener('mouseleave', mouseLeaveHandler)

  // 动画循环
  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 更新和绘制粒子
    particles.forEach((particle) => {
      particle.update()
      particle.draw(ctx)
    })

    // 绘制连接线
    connectParticles(ctx)

    animationId = requestAnimationFrame(animate)
  }
  animate()
}

function connectParticles(ctx) {
  for (let a = 0; a < particles.length; a++) {
    for (let b = a; b < particles.length; b++) {
      let dx = particles[a].x - particles[b].x
      let dy = particles[a].y - particles[b].y
      let distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < config.connectionDistance) {
        let opacity = 1 - distance / config.connectionDistance
        ctx.strokeStyle = `rgba(88, 166, 255, ${opacity * 0.2})`
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.moveTo(particles[a].x, particles[a].y)
        ctx.lineTo(particles[b].x, particles[b].y)
        ctx.stroke()
      }
    }
  }
}

onMounted(() => {
  initParticles()
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  if (mouseMoveHandler) window.removeEventListener('mousemove', mouseMoveHandler)
  if (mouseLeaveHandler) window.removeEventListener('mouseleave', mouseLeaveHandler)
})
</script>

<style scoped>
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: var(--z-base, 0);
}
</style>
