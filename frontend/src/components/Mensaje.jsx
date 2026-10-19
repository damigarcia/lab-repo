export default function Mensaje({ tipo = 'info', children }) {
  return <div className={`mensaje mensaje-${tipo}`}>{children}</div>;
}
