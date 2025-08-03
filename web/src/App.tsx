import { useEffect, useState } from 'react'
import './App.css'
import { RandomNumberControllerApi } from './runnify-api-v1/apis/RandomNumberControllerApi'

function App() {
  const [randomNumber, setRandomNumber] = useState(0);

  const randomNumberControllerApi = new RandomNumberControllerApi();

  const fetchAndSetRandomNumber = async () => {
    const response = await randomNumberControllerApi.randomNumber();
    setRandomNumber(response);
  }

  // Empty dependency array means this will only run once when the component mounts
  useEffect(() => {
    fetchAndSetRandomNumber();
  }, []);

  return (
    <>
     <div>Random Number: {randomNumber}</div>
     <button onClick={() => fetchAndSetRandomNumber()}>Generate Random Number</button>
    </>
  )
}

export default App
