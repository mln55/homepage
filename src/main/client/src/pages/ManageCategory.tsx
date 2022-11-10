import { useEffect } from 'react';
import CategoryEditItem from '../components/CategoryEditItem';
import CategoryItem from '../components/CategoryItem';
import { useDispatch, useSelector } from '../hooks/index';
import { CategoryApiResponse, categoryTreeActions, CategoryTreePayload } from '../slices/CategoryTreeSlice';
import { ApiResponse } from '../store/index';

function ManageCategory() {
  const categoryTree = useSelector(state => state.categoryTree.categoryTree);
  const updatable = useSelector(state => state.categoryTree.updatable);
  const editingCategoryIds = useSelector(state => state.categoryTree.editingCategoryIds);
  const newId = useSelector(state => state.categoryTree.newId);
  const totalPostsCount = categoryTree.reduce((sum, c) => sum + c.entries, 0);
  const dispatch = useDispatch();

  /**
   * 서버로부터 카테고리 정보를 받아 트리를 초기화한다.
   */
  useEffect(() => {
    (async () => {
      const data: ApiResponse<CategoryApiResponse[]> =
        await fetch('/api/categories?count=post').then(res => res.json());
      if (data.success) dispatch(categoryTreeActions.initCategoryTree(data.response!));
    })();
  }, []);

  // parent 하위로 카테고리를 추가한다.
  const handleAddClick = (parent: number | null) => {
    dispatch(categoryTreeActions.add({
      id: newId,
      parent: parent,
    }));
  }

  // 카테고리 편집을 마치고 서버와 통신한다.
  const handleSaveClick = () => {
    dispatch(categoryTreeActions.applyChange());
  }

  // 카테고리 편집을 시작한다.
  const handleUpdateClick = (id: number) => {
    dispatch(categoryTreeActions.updateStart(id));
  }

  // 카테고리 추가/수정을 취소한다.
  const handleUpdateReset = (data: CategoryTreePayload) => {
    dispatch(categoryTreeActions.updateReset(data));
  }

  // 카테고리 추가/수정을 확정한다.
  const handleUpdateSubmit = (data: CategoryTreePayload) => {
    dispatch(categoryTreeActions.updateName(data));
  }

  // 카테고리를 삭제한다.
  const handleDeleteClick = (data: CategoryTreePayload) => {
    dispatch(categoryTreeActions.delete(data));
  }

  return (
    <div className="category-container">
      <ul className="category-list">
        <li className="category-item category-root">
          <div className="category-content">
            <span className="category-name">전체 카테고리</span>
            <em>({totalPostsCount})</em>
          </div>
        </li>
        {
          categoryTree.length < 1
          ? null
          : categoryTree.map(c =>
            editingCategoryIds.indexOf(c.categoryId) > -1
            ? <CategoryEditItem
              key={c.categoryId}
              data={c}
              editingCategoryIds={editingCategoryIds}
              onUpdateReset={handleUpdateReset}
              onUpdateSubmit={handleUpdateSubmit}
              onAddClick={handleAddClick}
              onUpdateClick={handleUpdateClick}
              onDeleteClick={handleDeleteClick}
            />
            : <CategoryItem
              key={c.categoryId}
              data={c}
              editingCategoryIds={editingCategoryIds}
              onUpdateReset={handleUpdateReset}
              onUpdateSubmit={handleUpdateSubmit}
              onAddClick={handleAddClick}
              onUpdateClick={handleUpdateClick}
              onDeleteClick={handleDeleteClick}
            />
          )
        }
      </ul>
      <div className="category-list-button">
        <button className="category-add-btn" onClick={() => handleAddClick(null)} type="button">카테고리 추가</button>
        <button disabled={!updatable} onClick={handleSaveClick}  type="button">저장</button>
      </div>
    </div>
  )
}

export default ManageCategory;
