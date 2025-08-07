import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import HomePage from '../features/home/HomePage'
import SpotifyCallback from '../features/auth/SpotifyCallback'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/auth/spotify/callback" element={<SpotifyCallback />} />
      </Routes>
    </Router>
  )
}

export default App
