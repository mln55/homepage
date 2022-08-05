import { Link } from 'react-router-dom';

function Sidebar() {
  return (
    <ul className="manage-sidebar">
      <li><Link to={'/admin/manage'}>관리 홈</Link></li>
      <li><Link to={'/admin/manage/category'}>카테고리 관리</Link></li>
    </ul>
  )
}

export default Sidebar
