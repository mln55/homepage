import { FormEvent, useState } from "react";
import CategoryItem, { CategoryItemProps } from './CategoryItem';

// 편집 중인 카테고리에 대한 component
function CategoryEditItem (props: CategoryItemProps) {
  const [name, setName] = useState<string>(props.data.name);

  const handleReset = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    props.onUpdateReset({
      id: props.data.categoryId,
      name: props.data.name,
      parent: props.data.parent,
    });
  }

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!name.trim().length) return;

    props.onUpdateSubmit({
      id: props.data.categoryId,
      name: name,
      parent: props.data.parent
    });
  }

  return (
    <li className="category-edit" onMouseOver={e => e.stopPropagation()} onMouseOut={e => e.stopPropagation()}>
      <div>
        <form className="category-content" onSubmit={handleSubmit} onReset={handleReset}>
          <input className="category-name" autoFocus={true} type="text" value={name} onChange={(e) => {setName(e.target.value)}}/>
          <div className="category-item-button">
            <button type="reset">취소</button>
            <button disabled={name.trim().length ? false : true} type="submit">확인</button>
          </div>
        </form>
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

export default CategoryEditItem;
