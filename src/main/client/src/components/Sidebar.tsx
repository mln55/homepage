import { Link, useMatch } from 'react-router-dom';

function Sidebar() {

  const reloadRequired = !!useMatch('/admin/manage/newpost/*');

  return (
    <ul className="manage-sidebar">
      <li><Link to={'/admin/manage'} reloadDocument={reloadRequired}>관리 홈</Link></li>
      <li><Link to={'/admin/manage/post'} reloadDocument={reloadRequired}>게시글 관리</Link></li>
      <li><Link to={'/admin/manage/category'} reloadDocument={reloadRequired}>카테고리 관리</Link></li>
    </ul>
  )
}

export default Sidebar
