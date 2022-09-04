import { Provider } from 'react-redux';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Home from './components/Home';
import Sidebar from './components/Sidebar';
import ManageCategory from "./pages/ManageCategory";
import ManagePost from './pages/ManagePost';
import NewPost from './pages/NewPost';
import store from './store';

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Sidebar />
        <Routes>
          <Route path="admin/manage">
            <Route path="" element={<Home />}></Route>
            <Route path="post" element={<ManagePost />}></Route>
            <Route path="newpost" element={<NewPost />}>
              <Route path=":postIdStr" element={<NewPost />}></Route>
            </Route>
            <Route path="category" element={<ManageCategory />}></Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
  )
}

export default App
