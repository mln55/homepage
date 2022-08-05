import { createRoot } from 'react-dom/client';
import App from './App';

const root = createRoot(document.querySelector('#manage-wrapper')!);
root.render(<App />);
