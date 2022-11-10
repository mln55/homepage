import React from 'react';
import { useDispatch, useSelector } from '../hooks/index';
import { CategoryApiResponse } from '../slices/CategoryTreeSlice';
import { CategoryApiNameResponse, PostSliceActions } from '../slices/PostSlice';

const PostChangeOption = (props: {
  categoryTree: CategoryApiResponse[];
  isActivate: boolean;
  onToggleOpen: () => void;
  el: React.RefObject<HTMLDivElement>;
}) => {

  const {
    isActivate,
    onToggleOpen,
    el: ref,
    categoryTree
  } = props;

  const checkedPostIds = useSelector(state => state.post.checkedPostIds);
  const dispatch = useDispatch();

  const handleChangePostInfo = (info: {
    visible?: boolean,
    categoryId?: number,
    category?: CategoryApiNameResponse
  }) => {
    if (!isActivate) return;
    const { visible, categoryId, category } = info;
    if (visible === undefined && categoryId === undefined) return;

    checkedPostIds.forEach(id => {
      fetch(`/api/posts/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          'visible': visible,
          'categoryId': categoryId
        })
      }).then(res => res.json())
        .then(data => {
          if (data.success) {
            dispatch(PostSliceActions.updatePost({
              id: id,
              visible: visible!,
              category: category!
            }));
          }
        })
    });

    window.location.reload();
  }

  const handleChangeVisible = (visible: boolean) => {
    handleChangePostInfo({visible: visible});
  };

  const handleChangeCategory = (categoryId: number, category: CategoryApiNameResponse) => {
    handleChangePostInfo({categoryId: categoryId, category: category});
  };

  const handleDummyAClick = (e: MouseEvent) => {
    e.preventDefault();
  }

  return (
    <div className="change" ref={ref}>
      <button type="button" disabled={!isActivate} onClick={onToggleOpen}>변경</button>
      <div className="option">
        <span>공개 여부</span>
        <ul>
          <li onClick={() => handleChangeVisible(true)}><a href="" onClick={() => handleDummyAClick}>공개</a></li>
          <li onClick={() => handleChangeVisible(false)}><a href="" onClick={() => handleDummyAClick}>비공개</a></li>
        </ul>
        <span>카테고리</span>
        <ul>
          {
            categoryTree.map(p =>
              <React.Fragment key={p.categoryId}>
              <li onClick={() => handleChangeCategory(p.categoryId, {name: p.name, parent: null})}>
                <a href="" onClick={() => handleDummyAClick}>{p.name}</a>
                {p.childList.length < 1
                  ? null
                  : <ul>
                    {
                      p.childList.map(c =>
                        <li key={c.categoryId} onClick={() => handleChangeCategory(c.categoryId, {name: c.name, parent: p.name})}>
                          <a href="" onClick={() => handleDummyAClick}>{c.name}</a>
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

export default PostChangeOption;
