import { Link } from 'react-router-dom';
import { useSelector } from '../hooks/index';

const PostPagination = (props: {
  page: number,
  paramCategoryId: string | null,
  paramVisible: string | null,
}) => {

  const {
    page,
    paramCategoryId: categoryId,
    paramVisible: visible
  } = props;
  const totalCount = useSelector(state => state.post.totalCount);
  const defaultPageSize = 8;
  const defaultPageInterval = 5;
  const lastPage = Math.floor((totalCount - 1) / defaultPageSize) + 1;
  const startPage = Math.floor((page - 1) / defaultPageInterval) * defaultPageInterval + 1;
  const endPage = Math.min(lastPage, Math.floor((page - 1) / defaultPageInterval) * defaultPageInterval + defaultPageInterval);

  return totalCount === 0 ? null : (
    <div className="pagination">
    {
      startPage > defaultPageInterval
        ? <Link to={`${categoryId ? `?categoryid=${categoryId}&` : '?'}page=${startPage - 1}${visible ? `&visible=${visible}` : ''}`}>이전</Link>
        : null
    }
    <ul className='pagination'>
      {
        Array(endPage - startPage + 1).fill(0).map((v, i) =>
          <li key={i}>
            <Link className={page === startPage + i ? 'on' : ''} to={`${categoryId ? `?categoryid=${categoryId}&` : '?'}page=${startPage + i}${visible ? `&visible=${visible}` : ''}`}>{startPage + i}</Link>
          </li>
        )
      }
    </ul>
    {
      endPage < lastPage
      ? <Link to={`${categoryId ? `?categoryid=${categoryId}&` : '?'}page=${endPage + 1}${visible ? `&visible=${visible}` : ''}`}>다음</Link>
      : null
    }
  </div>
  )
}

export default PostPagination;
