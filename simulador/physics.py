"""Modelo ideal diferencial, SI, yaw positivo anti-horário, frente +Y em yaw=0."""
from dataclasses import dataclass, field
import math


def rotate(x, y, angle):
    return x * math.cos(angle) - y * math.sin(angle), x * math.sin(angle) + y * math.cos(angle)


def trajectory(height, speed, angle_deg, distance):
    a = math.radians(angle_deg)
    if speed <= 0 or abs(math.cos(a)) < 1e-9:
        raise ValueError("Velocidade positiva e ângulo entre 0 e 89 graus são necessários")
    return height + distance * math.tan(a) - 9.81 * distance**2 / (2 * speed**2 * math.cos(a)**2)


@dataclass
class Motor:
    side: str = "aux"
    mount: int = 1
    direction: int = 1
    power: float = 0
    raw_ticks: float = 0
    zero: float = 0
    target: int = 0
    mode: str = "RUN_WITHOUT_ENCODER"
    brake: str = "FLOAT"

    def position(self):
        return round((self.raw_ticks - self.zero) * self.direction)

    def speed(self, maximum, ticks_per_m):
        if self.mode == "STOP_AND_RESET_ENCODER":
            return 0
        if self.mode == "RUN_TO_POSITION":
            error = self.target - (self.raw_ticks - self.zero) * self.direction
            if abs(error) <= 2:
                return 0
            return math.copysign(min(abs(self.power) * maximum, abs(error) / ticks_per_m / .01), error) * self.direction
        return self.power * self.direction * maximum


@dataclass
class World:
    track: float = .45
    width: float = .45
    length: float = .45
    max_speed: float = 1.0
    ticks_per_m: float = 1980.6
    x: float = 1.83
    y: float = .65
    yaw: float = 0
    elapsed: float = 0
    yaw_zero: float = 0
    motors: dict = field(default_factory=lambda: {"ME": Motor("left", 1), "MD": Motor("right", -1), "MS": Motor(), "MI": Motor()})
    trail: list = field(default_factory=list)

    def step(self, dt):
        sides = {"left": [], "right": []}
        for motor in self.motors.values():
            speed = motor.speed(self.max_speed, self.ticks_per_m)
            delta = speed * self.ticks_per_m * dt
            if motor.mode == "RUN_TO_POSITION":
                remaining = motor.target * motor.direction - (motor.raw_ticks - motor.zero)
                delta = math.copysign(min(abs(delta), abs(remaining)), remaining) if speed else 0
                speed = delta / self.ticks_per_m / dt
            motor.raw_ticks += delta
            if motor.side in sides:
                sides[motor.side].append(speed * motor.mount)
        vl = sum(sides["left"]) / max(1, len(sides["left"]))
        vr = sum(sides["right"]) / max(1, len(sides["right"]))
        v, w = (vl + vr) / 2, (vr - vl) / self.track
        # Integração exata de arco: frente do robô = vetor (-sin(yaw), cos(yaw)).
        heading = self.yaw + math.pi / 2
        if abs(w) < 1e-10:
            self.x += v * math.cos(heading) * dt
            self.y += v * math.sin(heading) * dt
        else:
            self.x += v / w * (math.sin(heading + w * dt) - math.sin(heading))
            self.y += v / w * (-math.cos(heading + w * dt) + math.cos(heading))
        self.yaw = (self.yaw + w * dt + math.pi) % (2 * math.pi) - math.pi
        self.elapsed += dt
        if not self.trail or math.hypot(self.x-self.trail[-1][0], self.y-self.trail[-1][1]) > .01:
            self.trail.append([self.x, self.y])
            self.trail = self.trail[-3000:]

    def snapshot(self):
        return {"x": self.x, "y": self.y, "yaw": math.degrees(self.yaw), "time": self.elapsed,
                "width": self.width, "length": self.length, "trail": self.trail[:],
                "motors": {name: {"power": m.power, "encoder": m.position(), "direction": m.direction,
                                  "mount": m.mount, "side": m.side, "mode": m.mode} for name, m in self.motors.items()}}
