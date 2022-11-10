import { useEffect } from "react";

function Home() {

  useEffect(() => {
    document.title = '관리 홈';
  }, []);

  return (
    <div>
      <h2>manage home</h2>
    </div>
  )
}

export default Home
