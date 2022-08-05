import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface CategoryApiResponse {
  categoryId: number;
  name: string;
  parentId: number | null;
  postsCount: number;
  childList: CategoryApiResponse[];
}

export interface Category {
  categoryId: number;
  name: string;
  parent: number | null;
  entries: number;
  childList: Category[];
}

export type CategoryTreeState = {
  init: boolean;
  categoryTree: Category[];
  updatable: boolean,
  newId: number;
  addedCategoryIds: number[];
  updatedCategoryIds: number[];
  deletedCategoryIds: number[];
  editingCategoryIds: number[];
}

export type CategoryTreePayload = {
  id: number;
  name?: string;
  parent: number | null;
}

class TreeHelper {
  static findNode = (tree: Category[], id: number): Category | null => {
    return this.traverse(tree, (node => node.categoryId === id ? node : undefined));
  }

  static traverse = (tree: Category[], callback: (node: Category) => any): any => {
    for (let p of tree) {
      let r = callback(p);
      if (r !== undefined) return r;
      for (let c of p.childList) {
        r = callback(c);
        if (r !== undefined) return r;
      }
    }
  }

  static isUpdatable = (state: CategoryTreeState) => {
    return state.addedCategoryIds.length > 0 ||
    state.updatedCategoryIds.length > 0 ||
    state.deletedCategoryIds.length > 0;
  }
}

const CategoryTreeSlice = createSlice({
  name: 'categoryTreeSlice',

  initialState: {
    init: false,
    categoryTree: [],
    updatable: false,
    newId: -1,
    addedCategoryIds:  [],
    updatedCategoryIds: [],
    deletedCategoryIds: [],
    editingCategoryIds: []
  } as CategoryTreeState,

  reducers: {

    // 서버로부터 받은 정보로 categoryTree 초기화한다.
    initCategoryTree: (state: CategoryTreeState, action: PayloadAction<CategoryApiResponse[]>) => {
      if (state.init) return;

      // parent loop
      action.payload.forEach(c => {
        const category: Category = {
          categoryId: c.categoryId,
          name: c.name,
          parent: c.parentId,
          entries: c.postsCount,
          childList: []
        };

        // append child to parent
        for (let child of c.childList) {
          category.childList.push({
            categoryId: child.categoryId,
            name: child.name,
            parent: child.parentId,
            entries: child.postsCount,
            childList: []
          });
        }
        state.categoryTree.push(category);
      });
      state.init = true;
    },

    // node id를 받아 edit list에 추가한다.
    startEdit: (state: CategoryTreeState, action: PayloadAction<number>) => {
      state.editingCategoryIds.push(action.payload);
      state.updatable = TreeHelper.isUpdatable(state);
    },

    // node id를 받아 edit list에서 삭제한다.
    endEdit: (state: CategoryTreeState, action: PayloadAction<number>) => {
      state.editingCategoryIds.splice(state.editingCategoryIds.indexOf(action.payload), 1);
      state.updatable = TreeHelper.isUpdatable(state);
    },

    // 새로운 노드를 categoryTree에 추가한다.
    add: (state: CategoryTreeState, action: PayloadAction<CategoryTreePayload>) => {
      // id 감소
      --state.newId;

      const id = action.payload.id;
      const parent = action.payload.parent;

      const category: Category = {
        categoryId: id,
        name: '',
        parent: parent,
        entries: 0,
        visibleCount: 0,
        invisibleCount: 0,
        childList: []
      };

      // parent O -> append to childList
      if (parent) {
        state.categoryTree.find(c => c.categoryId === parent)!.childList.push(category);

      // parent X -> append to tree
      } else {
        state.categoryTree.push(category);
      }

      // 리스트에 추가
      state.addedCategoryIds.push(id);

      // 편집 시작
      CategoryTreeSlice.caseReducers.startEdit(state, {
        payload: action.payload.id,
        type: action.type
      });
    },

    // node id를 받아 편집을 시작한다.
    updateStart: (state: CategoryTreeState, action: PayloadAction<number>) => {
      // 편집을 시작한다.
      CategoryTreeSlice.caseReducers.startEdit(state, action);
    },

    updateReset: (state: CategoryTreeState, action: PayloadAction<CategoryTreePayload>) => {
      const name = action.payload.name;

      // name O -> 수정 중인 카테고리. 편집을 종료하고 이전 상태로 되돌린다.
      if (name) {
        CategoryTreeSlice.caseReducers.endEdit(state, {
          payload: action.payload.id,
          type: action.type
        });

      // name X -> 새롭게 추가하는 카테고리. component를 제거한다.
      } else {
        CategoryTreeSlice.caseReducers.delete(state, action);
      }
    },

    // node 이름을 변경한다.
    updateName: (state: CategoryTreeState, action: PayloadAction<CategoryTreePayload>) => {
      const name = action.payload.name;
      if (!name) return;

      const id = action.payload.id;
      const parent = action.payload.parent;

      // 카테고리명 중복 확인
      const sameNameNode = parent
        ? state.categoryTree.find(p => p.categoryId === parent)?.childList.find(c => c.name === name)
        : state.categoryTree.find(p => p.name === name);

      if (sameNameNode && sameNameNode.categoryId != id) {
        alert('같은 이름의 카테고리가 존재합니다.');
        return;
      }

      // 카테고리 찾기
      const category = TreeHelper.findNode(state.categoryTree, id);

      // 수정
      if (!category) return;
      category.name = name;

      // 리스트 추가
      if (id > 0) state.updatedCategoryIds.push(id);

      // 편집 끝
      CategoryTreeSlice.caseReducers.endEdit(state, {
        payload: action.payload.id,
        type: action.type
      });
    },

    // node를 tree에서 제거한다.
    delete: (state: CategoryTreeState, action: PayloadAction<CategoryTreePayload>) => {
      const id = action.payload.id;
      const parent = action.payload.parent;

      // 포스트가 등록된 카테고리면 삭제 할 수 없다.
      const targetNode = TreeHelper.findNode(state.categoryTree, id);

      if (targetNode && targetNode.entries !== 0) {
        alert('게시글이 있는 카테고리는 삭제할 수 없습니다.');
        return;
      }

      // 트리에서 삭제
      if (parent) {
        const childList = state.categoryTree.find(p => p.categoryId === parent)!.childList;
        if (childList) childList.splice(childList.findIndex(c => c.categoryId === id), 1);
      } else {
        state.categoryTree.splice(state.categoryTree.findIndex(c => c.categoryId === id), 1);
      }

      // 새로 추가된 카테고리
      if (id < 0) state.addedCategoryIds.splice(state.addedCategoryIds.indexOf(id), 1);

      // 기존 카테고리
      else state.deletedCategoryIds.push(id);

      // 편집 끝
      CategoryTreeSlice.caseReducers.endEdit(state, {
        payload: action.payload.id,
        type: action.type
      });
    },

    // 변경 사항을 확정하고 서버와 통신한다.
    // TODO - 개별 요청이 아닌 단일 요청으로 변경(?)
    applyChange: (state: CategoryTreeState) => {
      if (!state.updatable) return;

      if (state.editingCategoryIds.length) {
        alert('편집 중인 카테고리가 있습니다.');
        return;
      }

      // delete
      state.deletedCategoryIds.forEach(i => {
        fetch('/api/categories/' + i, {
          method: 'DELETE',
        });
      });

      // update
      state.updatedCategoryIds.forEach(i => {
        const target = TreeHelper.findNode(state.categoryTree, i);

        // update
        if (target) {
          fetch('/api/categories/' + target.categoryId, {
            method: 'PATCH',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              name: target.name,
              parentId: target.parent
            })
          });
        }
      });

      // add
      /*
        TODO -
        새롭게 추가된 상위 카테고리에 하위 카테고리 추가 시
        parentId 가 음수로 들어가므로 서버에서 상위 카테고리를 찾을 수 없다.
        1. 서버에서 parent category id를 받아와 parentId를 대체한다.
        2. parentId를 음수로 넘겨주고 서버에서 처리한다.
      */
      state.addedCategoryIds.forEach(i => {
        const target = TreeHelper.findNode(state.categoryTree, i);

        if (target) {
          fetch('/api/categories', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              name: target.name,
              parentId: target.parent
            })
          });
        }
      });

      alert('변경 사항이 저장 되었습니다.');
    }
  },
});

export const categoryTreeActions = CategoryTreeSlice.actions;
export default CategoryTreeSlice;
