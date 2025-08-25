import type { ReactNode } from 'react';
import styles from './GenericListItem.module.css';

export interface GenericListItemProps {
  children?: ReactNode;
  className?: string;

  imageUrl?: string;
  title?: string;
  subtitle?: string;
  
  variant?: 'default' | 'selected';
  onClick?: () => void;
}

const GenericListItem = ({ 
  children, 
  className = '', 
  title, 
  imageUrl,
  subtitle,
  variant = 'default',
  onClick 
}: GenericListItemProps) => {
  console.log("imageUrl", imageUrl);
  const cardClasses = [
    styles.card,
    styles[variant],
    className,
    onClick ? styles.clickable : ''
  ].filter(Boolean).join(' ');

  return (
    <div className={cardClasses} onClick={onClick}>
      {/* Image */}
      <div className={styles.imageContainer}>
        {imageUrl ? <img src={imageUrl} alt={title} /> : <div className={styles.imagePlaceholder} />}
      </div>

      {/* Text */}
      <div className={styles.textContainer}>
        {title && <h3 className={styles.title}>{title}</h3>}
        {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
      </div>

      {/* Children */}
      <div className={styles.childrenContainer}>
        {children}
      </div>
    </div>
  );
};

export default GenericListItem;
