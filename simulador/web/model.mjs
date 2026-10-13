export function rotate(x, y, degrees) {
  const a = degrees * Math.PI / 180;
  return [x * Math.cos(a) - y * Math.sin(a), x * Math.sin(a) + y * Math.cos(a)];
}
export function launcher(pose, lateral, forward) {
  const [x, y] = rotate(lateral, forward, pose.yaw);
  return [pose.x + x, pose.y + y];
}
export function heightAt(distance, height, speed, elevation) {
  const a = elevation * Math.PI / 180;
  if (!(speed > 0) || elevation < 0 || elevation >= 90) throw new Error('Lançamento inválido');
  return height + distance * Math.tan(a) - 9.81 * distance ** 2 / (2 * speed ** 2 * Math.cos(a) ** 2);
}
export function flight(height, speed, elevation) {
  const a = elevation * Math.PI / 180;
  const vy = speed * Math.sin(a);
  const time = (vy + Math.sqrt(vy ** 2 + 2 * 9.81 * height)) / 9.81;
  return {time, range: speed * Math.cos(a) * time};
}
export function gamepadValues(pad) {
  const zero = Array(20).fill(0);
  if (!pad || pad.mapping !== 'standard') return zero;
  const button = n => Number(Boolean(pad.buttons[n]?.pressed));
  return [0, 1, 2, 3].map(i => pad.axes[i] || 0).concat(
    [pad.buttons[6]?.value || 0, pad.buttons[7]?.value || 0],
    [0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13, 14, 15].map(button));
}
