import { useState } from 'react';
import { FaRegBookmark, FaRegCalendar, FaRegEye, FaRegEyeSlash } from 'react-icons/fa';
import { MdCheckBox, MdCheckBoxOutlineBlank } from 'react-icons/md';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from '../hooks/index';
import { PostApiResponse, PostSliceActions } from '../slices/PostSlice';

function Post(props: {
  post: PostApiResponse;
  recivePostList: () => void;
}) {

  const { post, recivePostList } = props;
  const checkedPostIds = useSelector(state => state.post.checkedPostIds);
  const [mouseOver, setMouseOver] = useState<boolean>(false);
  const checked = checkedPostIds.indexOf(post.id) > -1;

  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleUpdateClick = () => {
    navigate(`/admin/manage/newpost/${post.id}`);
  }

  const handleDeleteClick = () => {
    if (confirm(`글 '${post.title}'을 삭제하시겠습니까?`)) {
      fetch(`/api/posts/${post.id}`, {
        method: 'DELETE'
      }).then(res => res.json())
        .then(data => {
          if (data.success) {
            dispatch(PostSliceActions.deletePost(post.id));
            recivePostList();
          }
        });
    }
  }

  const handleCheckPost = () => {
    if (checked) dispatch(PostSliceActions.uncheckOne(post.id));
    else dispatch(PostSliceActions.checkOne(post.id));
  }

  return (
    <li className="post-item" onMouseOver={() => setMouseOver(true)} onMouseOut={() => setMouseOver(false)}>
      <div>
        <input id={`postcheck${post.id}`} checked={checked} type="checkbox" onChange={handleCheckPost}/>
        {/* <label htmlFor={`postcheck${post.id}`}>
        {
          checked ? <MdCheckBox /> : <MdCheckBoxOutlineBlank />
        }
        </label> */}
      </div>
      <div className="post">
        <div className="title"><Link target="_blank" title={post.title} to={`/${post.id}`}>{post.title}</Link></div>
        <div className="date"><FaRegCalendar /> {post.postAt.slice(0, 10).replaceAll('-', '.')}</div>
        <div className="category">
          <FaRegBookmark />
          {
            post.category.parent
            ? <Link target="_blank" title={`${post.category.parent}/${post.category.name}`} to={`/category/${post.category.parent}/${post.category.name}`}>
              {post.category.parent}/{post.category.name}
            </Link>
            : <Link target="_blank" title={post.category.name} to={`/category/${post.category.name}`}>
              {post.category.name}
            </Link>
          }
        </div>
        <div className="visible">
          {
            post.visible
            ? <><FaRegEye /> '공개'</>
            : <><FaRegEyeSlash /> '비공개'</>
          }
        </div>
      </div>
      {
        mouseOver
        ? <div className="post-item-button">
          <button type="button" onClick={handleUpdateClick}>수정</button>
          <button type="button" onClick={handleDeleteClick}>삭제</button>
        </div>
        : null
      }
    </li>
  )
}

export default Post
