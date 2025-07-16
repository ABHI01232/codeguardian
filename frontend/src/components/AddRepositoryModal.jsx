import React, { useState } from 'react';
import { X, Github, Gitlab, Plus, Check } from 'lucide-react';

const AddRepositoryModal = ({ isOpen, onClose, onSubmit }) => {
  const [formData, setFormData] = useState({
    url: '',
    platform: 'GITHUB',
    name: '',
    description: '',
    defaultBranch: 'main',
    isPrivate: false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const platforms = [
    { value: 'GITHUB', label: 'GitHub', icon: Github },
    { value: 'GITLAB', label: 'GitLab', icon: Gitlab }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Validate URL
      if (!formData.url.trim()) {
        throw new Error('Repository URL is required');
      }

      // Extract repository name from URL if not provided
      if (!formData.name.trim()) {
        const urlParts = formData.url.split('/');
        const repoName = urlParts[urlParts.length - 1].replace('.git', '');
        formData.name = repoName;
      }

      await onSubmit(formData);
      
      // Reset form
      setFormData({
        url: '',
        platform: 'GITHUB',
        name: '',
        description: '',
        defaultBranch: 'main',
        isPrivate: false
      });
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to add repository');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleUrlChange = (e) => {
    const url = e.target.value;
    setFormData(prev => ({ ...prev, url }));

    // Auto-detect platform from URL
    if (url.includes('github.com')) {
      setFormData(prev => ({ ...prev, platform: 'GITHUB' }));
    } else if (url.includes('gitlab.com')) {
      setFormData(prev => ({ ...prev, platform: 'GITLAB' }));
    }

    // Auto-extract repository name
    if (url.includes('/')) {
      const urlParts = url.split('/');
      const repoName = urlParts[urlParts.length - 1].replace('.git', '');
      if (repoName && !prev.name) {
        setFormData(prev => ({ ...prev, name: repoName }));
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">Add Repository</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-md">
              <p className="text-sm text-red-600">{error}</p>
            </div>
          )}

          {/* Repository URL */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Repository URL *
            </label>
            <input
              type="url"
              name="url"
              value={formData.url}
              onChange={handleUrlChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="https://github.com/username/repository"
              required
            />
          </div>

          {/* Platform Selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Platform
            </label>
            <div className="grid grid-cols-2 gap-2">
              {platforms.map(platform => (
                <button
                  key={platform.value}
                  type="button"
                  onClick={() => setFormData(prev => ({ ...prev, platform: platform.value }))}
                  className={`flex items-center justify-center space-x-2 px-3 py-2 rounded-md border transition-colors ${
                    formData.platform === platform.value
                      ? 'border-primary-500 bg-primary-50 text-primary-700'
                      : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <platform.icon className="w-4 h-4" />
                  <span className="text-sm">{platform.label}</span>
                  {formData.platform === platform.value && (
                    <Check className="w-4 h-4" />
                  )}
                </button>
              ))}
            </div>
          </div>

          {/* Repository Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Repository Name
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="Auto-detected from URL"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={2}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="Optional description"
            />
          </div>

          {/* Default Branch */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Default Branch
            </label>
            <input
              type="text"
              name="defaultBranch"
              value={formData.defaultBranch}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="main"
            />
          </div>

          {/* Private Repository */}
          <div className="flex items-center">
            <input
              type="checkbox"
              name="isPrivate"
              checked={formData.isPrivate}
              onChange={handleChange}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label className="ml-2 text-sm text-gray-700">
              Private repository
            </label>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-md hover:bg-primary-700 disabled:opacity-50 flex items-center space-x-2"
            >
              {loading ? (
                <>
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  <span>Adding...</span>
                </>
              ) : (
                <>
                  <Plus className="w-4 h-4" />
                  <span>Add Repository</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddRepositoryModal;