import { MouseEvent, useState } from "react";
import { Category, CategoryTreePayload } from '../slices/CategoryTreeSlice';
import CategoryEditItem from "./CategoryEditItem";

export type CategoryItemProps = {
  data: Category;
  editingCategoryIds: number[];
  onUpdateSubmit: (data: CategoryTreePayload) => void;
  onUpdateReset: (data: CategoryTreePayload) => void;
  onAddClick: (parent: number) => void;
  onUpdateClick: (id: number) => void;
  onDeleteClick: (data: CategoryTreePayload) => void;
}

// 등록된 카테고리에 대한 component
function CategoryItem (props: CategoryItemProps) {

  const [mouseOver, setMouseOver] = useState<boolean>(false);

  const handleMouseOver = (e: MouseEvent<HTMLLIElement>) => {
    e.stopPropagation();
    setMouseOver(true);
  }

  const handleMouseOut = (e: MouseEvent<HTMLLIElement>) => {
    e.stopPropagation();
    setMouseOver(false);
  }

  const handleAddClick = () => {
    props.onAddClick(props.data.categoryId);
  }

  const handleUpdateClick = () => {
    props.onUpdateClick(props.data.categoryId);
  }

  const handleDeleteClick = () => {
    props.onDeleteClick({
      id: props.data.categoryId,
      parent: props.data.parent
    });
  }

  return (
    <li className="category-item" onMouseOver={handleMouseOver} onMouseOut={handleMouseOut}>
      <div className="category-content">
        <span className="category-name">{props.data.name}</span>
        <em>({props.data.entries})</em>
        {
          mouseOver
          ? <div className="category-item-button">
            {props.data.parent ? null : <button onClick={handleAddClick} type="button">추가</button>}
            <button onClick={handleUpdateClick} type="button">수정</button>
            <button onClick={handleDeleteClick} type="button">삭제</button>
          </div>
          : null
        }
      </div>
      { // 하위 카테고리
        props.data.childList.length < 1
        ? null
        : <ul className="subcategory-list">
          {
            props.data.childList.map(pcc =>
              props.editingCategoryIds.indexOf(pcc.categoryId) > -1
              ? <CategoryEditItem
                key={pcc.categoryId}
                data={pcc}
                editingCategoryIds={props.editingCategoryIds}
                onUpdateReset={props.onUpdateReset}
                onUpdateSubmit={props.onUpdateSubmit}
                onAddClick={props.onAddClick}
                onUpdateClick={props.onUpdateClick}
                onDeleteClick={props.onDeleteClick}
                />
              : <CategoryItem
                key={pcc.categoryId}
                data={pcc}
                editingCategoryIds={props.editingCategoryIds}
                onUpdateReset={props.onUpdateReset}
                onUpdateSubmit={props.onUpdateSubmit}
                onAddClick={props.onAddClick}
                onUpdateClick={props.onUpdateClick}
                onDeleteClick={props.onDeleteClick}
              />
            )
          }
        </ul>
      }
    </li>
  )
}

export default CategoryItem;
