import React, { useRef } from 'react';

/* Material-like button with ripple effect
   Props: variant (default|outlined|flat), onClick, children, className */
export default function MDButton({ variant='default', className='', onClick, children, type='button', disabled=false }) {
  const btnRef = useRef(null);

  const handleClick = (e) => {
    const button = btnRef.current;
    if (!button) return;
    const ripple = document.createElement('span');
    const size = Math.max(button.offsetWidth, button.offsetHeight);
    const rect = button.getBoundingClientRect();
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
    ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
    ripple.className = 'md-ripple';
    button.appendChild(ripple);
    setTimeout(() => { ripple.remove(); }, 600);
    onClick && onClick(e);
  };

  const variantClass = variant === 'outlined' ? 'outlined' : variant === 'flat' ? 'flat' : '';

  return (
    <button
      ref={btnRef}
      type={type}
      disabled={disabled}
      onClick={handleClick}
      className={`md-btn ${variantClass} ${disabled ? 'opacity-60 cursor-not-allowed' : ''} ${className}`.trim()}
    >
      {children}
    </button>
  );
}
