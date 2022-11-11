import React from 'react';
import { Link } from 'react-router-dom';
import { CategoryApiResponse } from '../slices/CategoryTreeSlice';

const PostViewOption = (props: {
  paramCategoryId: string | null,
  paramVisible: string | null,
  categoryTree: CategoryApiResponse[],
  onToggleOpen: () => void,
  el: React.RefObject<HTMLDivElement>;
}) => {

  const {
    onToggleOpen,
    el: ref,
    categoryTree,
    paramCategoryId: categoryId,
    paramVisible: visible
  } = props;

  return (
    <div className={'search'} ref={ref}>
      <button type="button" onClick={onToggleOpen}>보기</button>
      <div className="option">
        <Link className={!categoryId && !visible ? 'on' : ''} onClick={onToggleOpen} to="?">모든 글 보기</Link>
        <br />
        <span className="option-label">공개 여부</span>
        <ul>
          <li><Link className={visible ? '' : 'on'} onClick={onToggleOpen} to={`${categoryId ? `?categoryid=${categoryId}` : ''}`}>전체</Link></li>
          <li><Link className={visible === 'true' ? 'on' : ''} onClick={onToggleOpen} to={`${categoryId ? `?categoryid=${categoryId}&` : '?'}visible=true`}>공개</Link></li>
          <li><Link className={visible === 'false' ? 'on' : ''} onClick={onToggleOpen} to={`${categoryId ? `?categoryid=${categoryId}&` : '?'}visible=false`}>비공개</Link></li>
        </ul>
        <span className="option-label">카테고리</span>
        <ul>
          <li>
            <Link className={categoryId ? '' : 'on'} onClick={onToggleOpen} to={`?${visible ? `visible=${visible}` : ''}`}>전체 카테고리</Link>
          </li>
          {
            categoryTree.map(p =>
              <React.Fragment key={p.categoryId}>
              <li>
                <Link className={categoryId === '' + p.categoryId ? 'on' : ''}
                      onClick={onToggleOpen} to={`?categoryid=${p.categoryId}${visible ? `&visible=${visible}` : ''}`}
                      title={`${p.name} (${p.postsCount})`}
                  >{p.name} ({p.postsCount})
                </Link>
                {p.childList.length < 1
                  ? null
                  : <ul>
                    {
                      p.childList.map(c =>
                        <li key={c.categoryId}>
                          <Link className={categoryId === '' + c.categoryId ? 'on' : ''}
                                onClick={onToggleOpen} to={`?categoryid=${c.categoryId}${visible ? `&visible=${visible}` : ''}`}
                                title={`${c.name} (${c.postsCount})`}
                            >{c.name} ({c.postsCount})
                          </Link>
                        </li>
                      )
                    }
                  </ul>}
              </li>
              </React.Fragment>
            )
          }
        </ul>
      </div>
    </div>
  )
}

export default PostViewOption;
